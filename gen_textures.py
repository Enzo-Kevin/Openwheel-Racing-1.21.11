#!/usr/bin/env python3
"""Generate 16x16 RGBA PNG textures for the Openwheel Racing mod.
Uses only stdlib (struct, zlib) — no Pillow required.
"""

import struct
import zlib
import random
import os

OUT_DIR = "src/main/resources/assets/openwheelracing/textures/block"


def write_png(path, pixels):
    """Write a 16x16 RGBA PNG using filter type 0 (None) per row.
    pixels: list of 16 rows, each row is 16 (R, G, B, A) tuples.
    """
    def chunk(tag, data):
        c = struct.pack(">I", len(data)) + tag + data
        return c + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)

    # PNG signature
    sig = b"\x89PNG\r\n\x1a\n"

    # IHDR: width=16, height=16, bit_depth=8, color_type=6 (RGBA), compression=0, filter=0, interlace=0
    ihdr_data = struct.pack(">IIBBBBB", 16, 16, 8, 6, 0, 0, 0)
    ihdr = chunk(b"IHDR", ihdr_data)

    # Raw image data: prepend filter byte 0x00 to each row
    raw = bytearray()
    for row in pixels:
        raw.append(0x00)  # filter type 0 = None
        for (r, g, b, a) in row:
            raw += bytes([r, g, b, a])

    idat = chunk(b"IDAT", zlib.compress(bytes(raw), 9))
    iend = chunk(b"IEND", b"")

    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(sig + ihdr + idat + iend)


def clamp(v, lo=0, hi=255):
    return max(lo, min(hi, v))


# ── 1. asphalt_track.png ────────────────────────────────────────────────────
def make_asphalt_track():
    rng = random.Random(42)
    pixels = []
    for y in range(16):
        row = []
        for x in range(16):
            base = 38
            # sparse speckles: ~20% of pixels get a slight nudge
            r = rng.random()
            if r < 0.10:
                v = rng.randint(28, 34)   # darker speckle
            elif r < 0.25:
                v = rng.randint(42, 50)   # lighter speckle
            else:
                v = base + rng.randint(-2, 2)
            v = clamp(v, 26, 52)
            row.append((v, v, v, 255))
        pixels.append(row)
    return pixels


# ── 2. pit_lane.png ─────────────────────────────────────────────────────────
def make_pit_lane():
    rng = random.Random(7)
    LINE_ROWS = {5, 10}
    LINE_ALPHA = int(255 * 0.80)  # ~80% opacity white
    pixels = []
    for y in range(16):
        row = []
        for x in range(16):
            if y in LINE_ROWS:
                row.append((255, 255, 255, LINE_ALPHA))
            else:
                base = 52
                v = base + rng.randint(-3, 3)
                v = clamp(v, 44, 60)
                row.append((v, v, v, 255))
        pixels.append(row)
    return pixels


# ── 3. kerb_side.png ────────────────────────────────────────────────────────
def make_kerb_side():
    rng = random.Random(13)
    RED_STRIPE = (160, 40, 40, 255)
    pixels = []
    for y in range(16):
        row = []
        for x in range(16):
            if y == 1 or y == 14:   # thin stripe near top and bottom
                row.append(RED_STRIPE)
            else:
                base = 90
                v = base + rng.randint(-4, 4)
                v = clamp(v, 80, 102)
                row.append((v, v, v, 255))
        pixels.append(row)
    return pixels


# ── 4. kerb_top.png ─────────────────────────────────────────────────────────
def make_kerb_top():
    """Alternating 2px-wide red/white columns (north-facing orientation)."""
    RED   = (220, 50,  50,  255)
    WHITE = (240, 240, 240, 255)
    pixels = []
    for y in range(16):
        row = []
        for x in range(16):
            # stripe index: every 2 columns alternates colour
            stripe = (x // 2) % 2
            row.append(RED if stripe == 0 else WHITE)
        pixels.append(row)
    return pixels


# ── 5. barrier.png ──────────────────────────────────────────────────────────
def make_barrier():
    BASE        = (55, 55, 60)
    BAND        = (75, 75, 80)
    EDGE_DARK   = (40, 40, 45)
    # Horizontal band spanning rows 7-8 (centre-ish)
    BAND_ROWS   = {7, 8}
    EDGE_ROWS   = {0, 1, 14, 15}
    pixels = []
    for y in range(16):
        row = []
        for x in range(16):
            if y in EDGE_ROWS:
                c = EDGE_DARK
            elif y in BAND_ROWS:
                c = BAND
            else:
                c = BASE
            row.append((c[0], c[1], c[2], 255))
        pixels.append(row)
    return pixels


# ── Main ────────────────────────────────────────────────────────────────────
TEXTURES = [
    ("asphalt_track.png", make_asphalt_track),
    ("pit_lane.png",      make_pit_lane),
    ("kerb_side.png",     make_kerb_side),
    ("kerb_top.png",      make_kerb_top),
    ("barrier.png",       make_barrier),
]

for filename, maker in TEXTURES:
    path = os.path.join(OUT_DIR, filename)
    pixels = maker()
    assert len(pixels) == 16, f"{filename}: expected 16 rows, got {len(pixels)}"
    for i, row in enumerate(pixels):
        assert len(row) == 16, f"{filename} row {i}: expected 16 pixels, got {len(row)}"
    write_png(path, pixels)
    size = os.path.getsize(path)
    print(f"  wrote {path}  ({size} bytes)")

print("Done.")
