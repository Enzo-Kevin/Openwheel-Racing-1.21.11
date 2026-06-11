#!/usr/bin/env python3
"""Generate 6 new/replacement 16x16 RGBA PNG textures for the Openwheel Racing mod.
Uses stdlib only: struct, zlib — no Pillow/PIL.
"""

import struct
import zlib
from pathlib import Path

OUT = Path("src/main/resources/assets/openwheelracing/textures/block")


# ---------------------------------------------------------------------------
# PNG writer helpers
# ---------------------------------------------------------------------------

def _chunk(tag: bytes, data: bytes) -> bytes:
    length = struct.pack(">I", len(data))
    crc = struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)
    return length + tag + data + crc


def write_png(path: Path, pixels: list) -> None:
    """Write a 16x16 RGBA PNG (colour type 6, filter type 0 per row)."""
    raw = bytearray()
    for row in pixels:
        raw.append(0x00)  # filter type: None
        for r, g, b, a in row:
            raw.extend([r, g, b, a])

    compressed = zlib.compress(bytes(raw), 9)

    ihdr_data = struct.pack(">IIBBBBB", 16, 16, 8, 6, 0, 0, 0)
    png = (
        b"\x89PNG\r\n\x1a\n"
        + _chunk(b"IHDR", ihdr_data)
        + _chunk(b"IDAT", compressed)
        + _chunk(b"IEND", b"")
    )
    path.write_bytes(png)
    print(f"  wrote {path.name}  ({len(png)} bytes)")


def make_pixels(fill):
    """Return a 16x16 grid filled with the given RGBA colour."""
    return [[fill] * 16 for _ in range(16)]


# ---------------------------------------------------------------------------
# 1. start_finish_top — 2x2 checkerboard, black/white
# ---------------------------------------------------------------------------
def start_finish_top():
    BLACK = (20,  20,  20,  255)
    WHITE = (235, 235, 235, 255)
    px = make_pixels(BLACK)
    for r in range(16):
        for c in range(16):
            if ((r // 2) + (c // 2)) % 2 == 0:
                px[r][c] = WHITE
            else:
                px[r][c] = BLACK
    return px


# ---------------------------------------------------------------------------
# 2. start_finish_side — dark asphalt + 1px white top edge
# ---------------------------------------------------------------------------
def start_finish_side():
    ASPH = (40,  40,  40,  255)
    EDGE = (220, 220, 220, 255)
    px = make_pixels(ASPH)
    for c in range(16):
        px[0][c] = EDGE
    return px


# ---------------------------------------------------------------------------
# 3. checkpoint — asphalt base with a north-pointing arrow
# ---------------------------------------------------------------------------
def checkpoint():
    BASE  = (42,  42,  42,  255)
    ARROW = (220, 220, 220, 255)
    px = make_pixels(BASE)

    # Triangle tip at row 3 cols 7-8; widen by 1 px/side each row to row 8
    for row in range(3, 9):
        spread = row - 3          # 0 → 5
        left  = 7 - spread
        right = 8 + spread
        for c in range(left, right + 1):
            px[row][c] = ARROW

    # 2-px-wide stem rows 9-12
    for row in range(9, 13):
        px[row][7] = ARROW
        px[row][8] = ARROW

    return px


# ---------------------------------------------------------------------------
# 4. crude_oil_deposit — stone/deepslate base + dark glossy oil pockets
# ---------------------------------------------------------------------------
def crude_oil_deposit():
    BASE      = (75, 75,  80, 255)
    OIL       = ( 8,  8,  10, 255)
    HIGHLIGHT = (40, 50,  55, 255)

    px = make_pixels(BASE)

    # Pocket 1 — top-left cluster
    for (r, c) in [(2,2),(2,3),(3,2),(3,3),(4,3)]:
        px[r][c] = OIL
    px[2][4] = HIGHLIGHT

    # Pocket 2 — centre-right cluster
    for (r, c) in [(7,10),(7,11),(8,10),(8,11),(9,11),(9,12)]:
        px[r][c] = OIL
    px[7][12] = HIGHLIGHT

    # Pocket 3 — lower-left cluster
    for (r, c) in [(11,3),(11,4),(12,3),(12,4),(12,5),(13,4)]:
        px[r][c] = OIL
    px[11][5] = HIGHLIGHT

    # Pocket 4 — small top-right spot
    for (r, c) in [(4,12),(4,13),(5,12)]:
        px[r][c] = OIL
    px[4][14] = HIGHLIGHT

    return px


# ---------------------------------------------------------------------------
# 5. car_assembly_workstation — steel-grey bench top with tool hints
# ---------------------------------------------------------------------------
def car_assembly_workstation():
    BASE  = (70,  75,  80, 255)
    DARK  = (50,  55,  60, 255)
    MED   = (55,  60,  65, 255)
    LIGHT = (90,  95, 100, 255)

    px = make_pixels(BASE)

    # Subtle bevel border
    for c in range(16):
        px[0][c]  = LIGHT
        px[15][c] = DARK
    for r in range(16):
        px[r][0]  = LIGHT
        px[r][15] = DARK

    # Two horizontal tool-line "slots"
    for c in range(2, 14):
        px[5][c]  = DARK
        px[10][c] = DARK

    # Short vertical bolt marks at cols 4 and 11, rows 7-8
    for r in range(7, 9):
        px[r][4]  = MED
        px[r][11] = MED

    # Corner bolt dots
    for (r, c) in [(3,3),(3,12),(12,3),(12,12)]:
        px[r][c] = MED

    return px


# ---------------------------------------------------------------------------
# 6. refinery — dark metal with chamber, hot spot, pipe row
# ---------------------------------------------------------------------------
def refinery():
    BASE    = (45, 45,  50, 255)
    CHAMBER = (75, 75,  80, 255)
    HOT     = (200,100,  20, 255)
    PIPE    = (35, 35,  40, 255)
    RIVETS  = (60, 60,  65, 255)

    px = make_pixels(BASE)

    # Pipe row at top (rows 0-1)
    for c in range(16):
        px[0][c] = PIPE
        px[1][c] = PIPE
    # Pipe highlights every 4 cols on row 0
    for c in range(0, 16, 4):
        px[0][c] = RIVETS

    # Chamber: cols 6-9, rows 3-10
    for r in range(3, 11):
        for c in range(6, 10):
            px[r][c] = CHAMBER

    # Hot-spot 2x2 at bottom of chamber (rows 9-10, cols 7-8)
    for r in range(9, 11):
        for c in range(7, 9):
            px[r][c] = HOT

    # Rivet dots on chamber corners
    for (r, c) in [(3,6),(3,9),(10,6),(10,9)]:
        px[r][c] = RIVETS

    return px


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main():
    OUT.mkdir(parents=True, exist_ok=True)

    tasks = [
        ("start_finish_top.png",        start_finish_top()),
        ("start_finish_side.png",        start_finish_side()),
        ("checkpoint.png",               checkpoint()),
        ("crude_oil_deposit.png",        crude_oil_deposit()),
        ("car_assembly_workstation.png", car_assembly_workstation()),
        ("refinery.png",                 refinery()),
    ]

    for filename, pixels in tasks:
        assert len(pixels) == 16, f"{filename}: got {len(pixels)} rows"
        for i, row in enumerate(pixels):
            assert len(row) == 16, f"{filename} row {i}: got {len(row)} pixels"
        write_png(OUT / filename, pixels)

    print("Done — 6 textures written.")


if __name__ == "__main__":
    main()
