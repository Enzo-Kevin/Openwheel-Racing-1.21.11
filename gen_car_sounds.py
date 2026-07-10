#!/usr/bin/env python3
"""Engine sound synthesiser – per-manufacturer F1 PU profiles."""
from __future__ import annotations

import math
import shutil
import struct
import subprocess
import sys
import wave
from dataclasses import dataclass
from pathlib import Path

ROOT = Path(__file__).resolve().parent
OUT = ROOT / "src/main/resources/assets/openwheelracing/sounds"
SAMPLE_RATE = 44_100
DURATION_SECONDS = 3.0
REFERENCE_RPM = 6_500.0
BASE_AMPLITUDE = 0.82


@dataclass
class PUProfile:
    """Per power-unit sound personality.

    low_freq  = RPM / 40  (combustion thump, dominant at low RPM)
    high_freq = RPM / 60  (harmonic scream, dominant at high RPM)
    low_gain  / high_gain scale each sample's output amplitude.
    Ferrari/Renault: balanced (both 1.0).
    Mercedes/RBPT:   high_freq louder – V6 turbo hybrid screams harder at the top.
    """
    name: str
    low_gain: float
    high_gain: float
    low_waveform: str
    high_waveform: str


PU_PROFILES: list[PUProfile] = [
    PUProfile("ferrari",  low_gain=1.00, high_gain=1.00, low_waveform="saw",    high_waveform="square"),
    PUProfile("renault",  low_gain=1.00, high_gain=1.00, low_waveform="saw",    high_waveform="square"),
    PUProfile("mercedes", low_gain=0.68, high_gain=1.10, low_waveform="square", high_waveform="saw"),
    PUProfile("rbpt",     low_gain=0.78, high_gain=1.00, low_waveform="square", high_waveform="saw"),
]

# ---------------------------------------------------------------------------
# Oscillator
# ---------------------------------------------------------------------------

def square(freq: float, t: float) -> float:
    """Band-limited square wave for the RPM/60 layer."""
    s = 0.0
    max_n = min(39, int(SAMPLE_RATE / (2.0 * freq)))
    for n in range(1, max_n + 1, 2):
        s += math.sin(2.0 * math.pi * freq * n * t) / n
    return s * (4.0 / math.pi)


def sawtooth(freq: float, t: float) -> float:
    """Band-limited sawtooth via additive synthesis (avoids aliasing clicks)."""
    s = 0.0
    max_n = min(40, int(SAMPLE_RATE / (2.0 * freq)))
    for n in range(1, max_n + 1):
        s += ((-1.0) ** (n + 1)) * math.sin(2.0 * math.pi * freq * n * t) / n
    return s * (2.0 / math.pi)


ODD_HARMONICS = [(3, 0.20), (5, 0.12), (7, 0.06)]


def oscillator(freq: float, t: float, waveform: str) -> float:
    if waveform == "square":
        return square(freq, t)
    saw = sawtooth(freq, t)
    harmonics = sum(w * math.sin(2.0 * math.pi * freq * n * t)
                    for n, w in ODD_HARMONICS)
    return saw * 0.78 + harmonics * 0.22


# ---------------------------------------------------------------------------
# Sub-bass layer
# ---------------------------------------------------------------------------

def sub_bass(freq: float, t: float) -> float:
    """Layered sub: quarter-fundamental sine + half-fundamental sine."""
    quarter = math.sin(2.0 * math.pi * (freq * 0.25) * t)
    half    = math.sin(2.0 * math.pi * (freq * 0.50) * t)
    return quarter * 0.65 + half * 0.35


# ---------------------------------------------------------------------------
# Biquad filter helpers (Direct Form I)
# ---------------------------------------------------------------------------

class Biquad:
    def __init__(self, b0: float, b1: float, b2: float, a1: float, a2: float) -> None:
        self.b0, self.b1, self.b2 = b0, b1, b2
        self.a1, self.a2 = a1, a2
        self.x1 = self.x2 = self.y1 = self.y2 = 0.0

    def process(self, x: float) -> float:
        y = self.b0 * x + self.b1 * self.x1 + self.b2 * self.x2 \
            - self.a1 * self.y1 - self.a2 * self.y2
        self.x2, self.x1 = self.x1, x
        self.y2, self.y1 = self.y1, y
        return y


def make_lowpass(cutoff_hz: float, q: float = 0.707) -> Biquad:
    w0 = 2.0 * math.pi * cutoff_hz / SAMPLE_RATE
    cos_w0 = math.cos(w0)
    alpha = math.sin(w0) / (2.0 * q)
    b1 = 1.0 - cos_w0
    b0 = b1 / 2.0
    b2 = b0
    a0 = 1.0 + alpha
    a1 = -2.0 * cos_w0
    a2 = 1.0 - alpha
    return Biquad(b0 / a0, b1 / a0, b2 / a0, a1 / a0, a2 / a0)


def make_bandpass(center_hz: float, q: float) -> Biquad:
    w0 = 2.0 * math.pi * center_hz / SAMPLE_RATE
    alpha = math.sin(w0) / (2.0 * q)
    b0 = alpha
    b1 = 0.0
    b2 = -alpha
    a0 = 1.0 + alpha
    a1 = -2.0 * math.cos(w0)
    a2 = 1.0 - alpha
    return Biquad(b0 / a0, b1 / a0, b2 / a0, a1 / a0, a2 / a0)


def make_highpass(cutoff_hz: float, q: float = 0.707) -> Biquad:
    w0 = 2.0 * math.pi * cutoff_hz / SAMPLE_RATE
    cos_w0 = math.cos(w0)
    alpha = math.sin(w0) / (2.0 * q)
    b0 = (1.0 + cos_w0) / 2.0
    b1 = -(1.0 + cos_w0)
    b2 = b0
    a0 = 1.0 + alpha
    a1 = -2.0 * cos_w0
    a2 = 1.0 - alpha
    return Biquad(b0 / a0, b1 / a0, b2 / a0, a1 / a0, a2 / a0)


# ---------------------------------------------------------------------------
# Saturation (soft-clip / tube-like)
# ---------------------------------------------------------------------------

def saturate(x: float, drive: float = 2.8) -> float:
    """Soft saturation via tanh with drive. drive>1 adds harmonics."""
    return math.tanh(x * drive) / math.tanh(drive)


# ---------------------------------------------------------------------------
# Simple Schroeder reverb
# ---------------------------------------------------------------------------

class CombFilter:
    def __init__(self, delay_samples: int, feedback: float, damping: float) -> None:
        self.buf = [0.0] * delay_samples
        self.feedback = feedback
        self.damping = damping
        self._pos = 0
        self._store = 0.0

    def process(self, x: float) -> float:
        out = self.buf[self._pos]
        self._store = out * (1.0 - self.damping) + self._store * self.damping
        self.buf[self._pos] = x + self._store * self.feedback
        self._pos = (self._pos + 1) % len(self.buf)
        return out


class AllpassFilter:
    def __init__(self, delay_samples: int, feedback: float) -> None:
        self.buf = [0.0] * delay_samples
        self.feedback = feedback
        self._pos = 0

    def process(self, x: float) -> float:
        buf_out = self.buf[self._pos]
        self.buf[self._pos] = x + buf_out * self.feedback
        self._pos = (self._pos + 1) % len(self.buf)
        return buf_out - x


def make_reverb():
    """Schroeder-style reverb (8 combs + 4 allpass)."""
    SR = SAMPLE_RATE
    combs = [
        CombFilter(int(SR * 0.0297), 0.84, 0.20),
        CombFilter(int(SR * 0.0371), 0.84, 0.20),
        CombFilter(int(SR * 0.0411), 0.84, 0.20),
        CombFilter(int(SR * 0.0437), 0.84, 0.20),
        CombFilter(int(SR * 0.0461), 0.82, 0.18),
        CombFilter(int(SR * 0.0491), 0.82, 0.18),
        CombFilter(int(SR * 0.0517), 0.82, 0.18),
        CombFilter(int(SR * 0.0541), 0.80, 0.18),
    ]
    allpasses = [
        AllpassFilter(int(SR * 0.0050), 0.5),
        AllpassFilter(int(SR * 0.0017), 0.5),
        AllpassFilter(int(SR * 0.0010), 0.5),
        AllpassFilter(int(SR * 0.0007), 0.5),
    ]
    return combs, allpasses


def reverb_process(x: float, combs, allpasses) -> float:
    out = sum(c.process(x) for c in combs)
    out /= len(combs)
    for ap in allpasses:
        out = ap.process(out)
    return out


# ---------------------------------------------------------------------------
# Envelope
# ---------------------------------------------------------------------------

def envelope(frame: int, total_frames: int) -> float:
    fade_frames = int(SAMPLE_RATE * 0.03)
    if frame < fade_frames:
        return frame / fade_frames
    if frame > total_frames - fade_frames:
        return (total_frames - frame) / fade_frames
    return 1.0


# ---------------------------------------------------------------------------
# Write WAV
# ---------------------------------------------------------------------------

def write_wav(path: Path, freq: float, gain: float = 1.0, waveform: str = "saw") -> None:
    total_frames = int(SAMPLE_RATE * DURATION_SECONDS)
    amplitude = BASE_AMPLITUDE * gain

    # Let high harmonics breathe – F1 scream lives above 1 kHz
    lp_body  = make_lowpass(freq * 18.0, q=0.75)
    lp_air   = make_lowpass(4_800.0, q=0.65)
    # 120 Hz HP instead of 240 – keeps the turbo whoosh in the 200-400 Hz range
    hp_body  = make_highpass(120.0, q=0.80)
    hp_rumble = make_highpass(30.0, q=0.60)

    lp_sub = make_lowpass(100.0, q=0.55)

    # Formant peaks shaped for turbo-hybrid F1:
    #   350 Hz  – turbo whoosh / intake rumble
    #   1700 Hz – nasal entry of the wail
    #   2900 Hz – the characteristic F1 scream peak
    #   4000 Hz – upper presence (replaces the 4600 Hz kart shriek)
    formants = [
        (make_bandpass(350.0,      q=2.5),  0.10),
        (make_bandpass(1_700.0,    q=4.0),  0.10),
        (make_bandpass(2_700.0,    q=3.4),  0.11),
        (make_bandpass(freq * 3.0, q=6.0),  0.05),
    ]

    combs, allpasses = make_reverb()
    WET = 0.07
    DRY = 1.0 - WET
    SUB_MIX  = 0.14
    SAT_DRIVE = 2.2

    samples: list[float] = []
    for frame in range(total_frames):
        t = frame / SAMPLE_RATE

        osc = oscillator(freq, t, waveform)
        formant_sig = sum(filt.process(osc) * g for filt, g in formants)
        sig = osc * 0.84 + formant_sig * 0.16

        sig = lp_body.process(sig)
        sig = hp_body.process(sig)
        sig = saturate(sig, SAT_DRIVE)
        sig = lp_air.process(sig)

        sub = lp_sub.process(sub_bass(freq, t)) * SUB_MIX
        sig = sig + sub

        sig = hp_rumble.process(sig)

        rev = reverb_process(sig * 0.5, combs, allpasses)
        sig = sig * DRY + rev * WET

        sig *= envelope(frame, total_frames) * amplitude
        samples.append(max(-1.0, min(1.0, sig)))

    with wave.open(str(path), "wb") as wav:
        wav.setnchannels(2)
        wav.setsampwidth(2)
        wav.setframerate(SAMPLE_RATE)
        for s in samples:
            amp = int(s * 32767)
            wav.writeframesraw(struct.pack("<hh", amp, amp))


# ---------------------------------------------------------------------------
# Encode + main
# ---------------------------------------------------------------------------

def run(command: list[str]) -> bool:
    try:
        subprocess.run(command, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
        return True
    except subprocess.CalledProcessError as exc:
        message = exc.stderr.strip() or exc.stdout.strip()
        if message:
            print(message, file=sys.stderr)
        return False


def convert_to_ogg(wav_path: Path, ogg_path: Path) -> bool:
    ffmpeg = shutil.which("ffmpeg")
    if ffmpeg and run([ffmpeg, "-y", "-loglevel", "error", "-i", str(wav_path),
                       "-c:a", "libvorbis", "-q:a", "5", str(ogg_path)]):
        return True
    oggenc = shutil.which("oggenc")
    if oggenc and run([oggenc, "-Q", "-q", "5", str(wav_path), "-o", str(ogg_path)]):
        return True
    return False


def main() -> int:
    OUT.mkdir(parents=True, exist_ok=True)
    missing_encoder = False

    low_freq  = REFERENCE_RPM / 40.0
    high_freq = REFERENCE_RPM / 60.0

    for pu in PU_PROFILES:
        for layer, freq, gain, waveform in (
            ("low",  low_freq,  pu.low_gain,  pu.low_waveform),
            ("high", high_freq, pu.high_gain, pu.high_waveform),
        ):
            name = f"car_engine_{pu.name}_{layer}"
            wav_path = OUT / f"{name}.wav"
            ogg_path = OUT / f"{name}.ogg"
            write_wav(wav_path, freq, gain, waveform)
            if convert_to_ogg(wav_path, ogg_path):
                wav_path.unlink()
                print(f"wrote {ogg_path.relative_to(ROOT)}")
            else:
                missing_encoder = True
                print(f"wrote preview {wav_path.relative_to(ROOT)}")

    print(f"reference RPM {REFERENCE_RPM:.0f}: "
          f"low={low_freq:.2f} Hz, high={high_freq:.2f} Hz")
    if missing_encoder:
        print("Install ffmpeg or oggenc to produce Minecraft-ready .ogg files.", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
