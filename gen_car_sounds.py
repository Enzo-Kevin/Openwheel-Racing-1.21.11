#!/usr/bin/env python3
from __future__ import annotations

import math
import shutil
import struct
import subprocess
import sys
import wave
from pathlib import Path

ROOT = Path(__file__).resolve().parent
OUT = ROOT / "src/main/resources/assets/openwheelracing/sounds"
SAMPLE_RATE = 44_100
DURATION_SECONDS = 3.0
REFERENCE_RPM = 6_500.0
AMPLITUDE = 0.70

SOUNDS = {
    "car_engine_low": [REFERENCE_RPM / 40.0],
    "car_engine_high": [REFERENCE_RPM / 60.0],
}


def square(freq: float, t: float) -> float:
    return 1.0 if math.sin(2.0 * math.pi * freq * t) >= 0.0 else -1.0


def envelope(frame: int, total_frames: int) -> float:
    fade_frames = int(SAMPLE_RATE * 0.02)
    if frame < fade_frames:
        return frame / fade_frames
    if frame > total_frames - fade_frames:
        return (total_frames - frame) / fade_frames
    return 1.0


def write_wav(path: Path, freqs: list[float]) -> None:
    total_frames = int(SAMPLE_RATE * DURATION_SECONDS)
    with wave.open(str(path), "wb") as wav:
        wav.setnchannels(2)
        wav.setsampwidth(2)
        wav.setframerate(SAMPLE_RATE)
        for frame in range(total_frames):
            t = frame / SAMPLE_RATE
            sample = sum(square(freq, t) for freq in freqs) / len(freqs)
            sample *= envelope(frame, total_frames) * AMPLITUDE
            amp = int(max(-1.0, min(1.0, sample)) * 32767)
            wav.writeframesraw(struct.pack("<hh", amp, amp))


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
    if ffmpeg and run([ffmpeg, "-y", "-loglevel", "error", "-i", str(wav_path), "-c:a", "libvorbis", "-q:a", "5", str(ogg_path)]):
        return True

    oggenc = shutil.which("oggenc")
    if oggenc and run([oggenc, "-Q", "-q", "5", str(wav_path), "-o", str(ogg_path)]):
        return True

    return False


def main() -> int:
    OUT.mkdir(parents=True, exist_ok=True)
    missing_encoder = False

    for name, freqs in SOUNDS.items():
        wav_path = OUT / f"{name}.wav"
        ogg_path = OUT / f"{name}.ogg"
        write_wav(wav_path, freqs)
        if convert_to_ogg(wav_path, ogg_path):
            wav_path.unlink()
            print(f"wrote {ogg_path.relative_to(ROOT)}")
        else:
            missing_encoder = True
            print(f"wrote preview {wav_path.relative_to(ROOT)}")

    print(f"reference RPM {REFERENCE_RPM:.0f}: low={REFERENCE_RPM / 40.0:.2f} Hz, high={REFERENCE_RPM / 60.0:.2f} Hz")
    if missing_encoder:
        print("Install ffmpeg or oggenc to produce Minecraft-ready .ogg files.", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
