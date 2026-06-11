#!/usr/bin/env python3
"""Generate 16x16 Minecraft-style pixel art PNG textures for racing car component items.
Uses only stdlib: struct, zlib. No PIL/Pillow required.
"""

import struct
import zlib
import os
from pathlib import Path

OUT_DIR = Path("src/main/resources/assets/openwheelracing/textures/item")


# ---------------------------------------------------------------------------
# PNG writing helpers
# ---------------------------------------------------------------------------

def write_png(path, pixels):
    """Write a 16x16 RGBA PNG (colour type 6) with filter type 0 (None) per row."""
    def chunk(tag, data):
        c = tag + data
        return struct.pack(">I", len(data)) + c + struct.pack(">I", zlib.crc32(c) & 0xFFFFFFFF)

    sig = b"\x89PNG\r\n\x1a\n"
    ihdr = chunk(b"IHDR", struct.pack(">IIBBBBB", 16, 16, 8, 6, 0, 0, 0))

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
    size = os.path.getsize(path)
    print(f"  wrote {path}  ({size} bytes)")


def blank():
    return [[(0, 0, 0, 0)] * 16 for _ in range(16)]


def px(img, r, c, color):
    if 0 <= r < 16 and 0 <= c < 16:
        img[r][c] = color


# ---------------------------------------------------------------------------
# 1. chassis.png — open-wheel car tub/frame
# ---------------------------------------------------------------------------

def gen_chassis():
    img = blank()
    outer = (30, 30, 35, 255)
    fill  = (50, 50, 55, 255)

    # Fill interior: rows 6-9, cols 3-12
    for r in range(6, 10):
        for c in range(3, 13):
            px(img, r, c, fill)

    # Top edge rows 5-6, cols 3-12
    for c in range(3, 13):
        px(img, 5, c, outer)
        px(img, 6, c, outer)

    # Bottom edge rows 9-10, cols 3-12
    for c in range(3, 13):
        px(img, 9, c, outer)
        px(img, 10, c, outer)

    # Left edge cols 2-3, rows 6-9
    for r in range(6, 10):
        px(img, r, 2, outer)
        px(img, r, 3, outer)

    # Right edge cols 12-13, rows 6-9
    for r in range(6, 10):
        px(img, r, 12, outer)
        px(img, r, 13, outer)

    # Diagonal corners to round the oval
    px(img, 5, 3, outer); px(img, 6, 2, outer)   # top-left
    px(img, 5, 12, outer); px(img, 6, 13, outer)  # top-right
    px(img, 10, 3, outer); px(img, 9, 2, outer)   # bottom-left
    px(img, 10, 12, outer); px(img, 9, 13, outer) # bottom-right

    # Vertical side struts at col 3 and col 12, rows 4-11
    for r in range(4, 12):
        px(img, r, 3, outer)
        px(img, r, 12, outer)

    write_png(OUT_DIR / "chassis.png", img)


# ---------------------------------------------------------------------------
# 2. engine.png — compact race engine block
# ---------------------------------------------------------------------------

def gen_engine():
    img = blank()
    gunmetal = (55, 55, 60, 255)
    top_lite  = (75, 75, 80, 255)
    exhaust   = (150, 150, 155, 255)
    border    = (40, 40, 44, 255)

    # 10x8 rectangle: rows 4-11, cols 3-12
    for r in range(4, 12):
        for c in range(3, 13):
            color = top_lite if r == 4 else gunmetal
            px(img, r, c, color)

    # Dark border: bottom row and left col
    for c in range(3, 13):
        px(img, 11, c, border)
    for r in range(4, 12):
        px(img, r, 3, border)

    # 2 exhaust pipes on right side: 1px wide, each 3px tall
    for r in range(5, 8):
        px(img, r, 13, exhaust)
        px(img, r, 14, exhaust)
    for r in range(8, 11):
        px(img, r, 13, exhaust)
        px(img, r, 14, exhaust)

    write_png(OUT_DIR / "engine.png", img)


# ---------------------------------------------------------------------------
# 3. gearbox.png — mechanical casing
# ---------------------------------------------------------------------------

def gen_gearbox():
    img = blank()
    base  = (65, 65, 70, 255)
    tooth = (90, 90, 95, 255)
    bolt  = (170, 170, 175, 255)
    dark  = (48, 48, 52, 255)

    # 8x8 square: rows 4-11, cols 5-12
    for r in range(4, 12):
        for c in range(5, 13):
            px(img, r, c, base)

    # Dark outline
    for c in range(5, 13):
        px(img, 4, c, dark)
        px(img, 11, c, dark)
    for r in range(4, 12):
        px(img, r, 5, dark)
        px(img, r, 12, dark)

    # 3 gear-tooth bumps on top edge (row 3): 2px wide each at cols 6, 8, 10
    for c in [6, 8, 10]:
        px(img, 3, c, tooth)
        px(img, 3, c + 1, tooth)

    # Silver bolt highlights at corners
    for (r, c) in [(4, 5), (4, 12), (11, 5), (11, 12)]:
        px(img, r, c, bolt)

    write_png(OUT_DIR / "gearbox.png", img)


# ---------------------------------------------------------------------------
# 4. steering_controls.png — steering wheel
# ---------------------------------------------------------------------------

def gen_steering_controls():
    img = blank()
    rim   = (80, 45, 20, 255)
    spoke = (60, 60, 65, 255)
    hub   = (190, 190, 195, 255)

    cx, cy  = 7.5, 7.5
    r_outer = 4.8
    r_inner = 3.3

    # Draw rim as annulus
    for r in range(16):
        for c in range(16):
            d = ((r - cy) ** 2 + (c - cx) ** 2) ** 0.5
            if r_inner <= d <= r_outer:
                px(img, r, c, rim)

    # Cross spokes: horizontal rows 7-8, vertical cols 7-8 (only where not rim)
    for c in range(3, 13):
        if img[7][c] == (0, 0, 0, 0):
            px(img, 7, c, spoke)
        if img[8][c] == (0, 0, 0, 0):
            px(img, 8, c, spoke)
    for r in range(3, 13):
        if img[r][7] == (0, 0, 0, 0):
            px(img, r, 7, spoke)
        if img[r][8] == (0, 0, 0, 0):
            px(img, r, 8, spoke)

    # Centre hub 2x2
    px(img, 7, 7, hub); px(img, 7, 8, hub)
    px(img, 8, 7, hub); px(img, 8, 8, hub)

    write_png(OUT_DIR / "steering_controls.png", img)


# ---------------------------------------------------------------------------
# 5. tires.png — stack of 2 black slick tyres
# ---------------------------------------------------------------------------

def gen_tires():
    img = blank()
    outer_c = (25, 25, 25, 255)
    inner_c = (45, 45, 45, 255)

    def draw_tyre(cy, cx, rx, ry, thickness):
        for r in range(16):
            for c in range(16):
                dx = (c - cx) / rx
                dy = (r - cy) / ry
                d = (dx**2 + dy**2) ** 0.5
                if d <= 1.0:
                    # check inner ellipse
                    irx = rx - thickness
                    iry = ry - thickness
                    if irx > 0 and iry > 0:
                        dx2 = (c - cx) / irx
                        dy2 = (r - cy) / iry
                        d2 = (dx2**2 + dy2**2) ** 0.5
                        if d2 > 1.0:
                            px(img, r, c, outer_c)  # tyre wall (outer ring)
                        else:
                            px(img, r, c, inner_c)  # hollow centre lighter
                    else:
                        px(img, r, c, outer_c)

    # Top tyre: rows 2-6 → centre row 4, cols full → cx=7.5, rx=6, ry=2
    draw_tyre(4.0, 7.5, 6.0, 2.0, 1.2)
    # Bottom tyre: rows 9-13 → centre row 11
    draw_tyre(11.0, 7.5, 6.0, 2.0, 1.2)

    write_png(OUT_DIR / "tires.png", img)


# ---------------------------------------------------------------------------
# 6. aero_kit.png — front/rear wing silhouette
# ---------------------------------------------------------------------------

def gen_aero_kit():
    img = blank()
    dark = (35, 35, 40, 255)
    mid  = (50, 50, 55, 255)

    # Front wing trapezoid (bottom half): rows 11-14
    # row 14: cols 1-14 (14px), row 13: cols 2-13 (12px),
    # row 12: cols 3-12 (10px), row 11: cols 4-11 (8px)
    trapezoid = [
        (14, 1, 15),
        (13, 2, 14),
        (12, 3, 13),
        (11, 4, 12),
    ]
    for (r, c_start, c_end) in trapezoid:
        for c in range(c_start, c_end):
            px(img, r, c, dark)

    # Thin centre strut connecting wings: cols 7-8, rows 4-11
    for r in range(4, 12):
        px(img, r, 7, mid)
        px(img, r, 8, mid)

    # Rear wing: rows 1-3, cols 2-13 (12px wide)
    for r in range(1, 4):
        for c in range(2, 14):
            px(img, r, c, dark)

    # Rear wing end plates: rows 1-4 at cols 2 and 13
    for r in range(1, 5):
        px(img, r, 2, dark)
        px(img, r, 13, dark)

    write_png(OUT_DIR / "aero_kit.png", img)


# ---------------------------------------------------------------------------
# 7. carbon_fiber.png — woven carbon weave pattern
# ---------------------------------------------------------------------------

def gen_carbon_fiber():
    img = blank()
    base  = (20, 20, 22, 255)
    weave = (38, 38, 42, 255)

    for r in range(16):
        for c in range(16):
            # Checkerboard in 2x2 blocks
            block_r = (r // 2) % 2
            block_c = (c // 2) % 2
            color = weave if (block_r + block_c) % 2 == 0 else base
            img[r][c] = color

    # Diagonal highlight: row==col or row==col+1 → +15 brightness
    for r in range(16):
        for c in range(16):
            if r == c or r == c + 1:
                p = img[r][c]
                img[r][c] = (
                    min(255, p[0] + 15),
                    min(255, p[1] + 15),
                    min(255, p[2] + 15),
                    255,
                )

    write_png(OUT_DIR / "carbon_fiber.png", img)


# ---------------------------------------------------------------------------
# 8. prototype_car_spawn.png — top-down F1 car view
# ---------------------------------------------------------------------------

def gen_prototype_car_spawn():
    img = blank()
    red       = (200, 30, 30, 255)
    highlight = (220, 50, 50, 255)
    wing_col  = (180, 180, 185, 255)
    black     = (15, 15, 15, 255)
    cockpit   = (20, 20, 20, 255)

    # Central body pod: cols 6-9, rows 3-12
    for r in range(3, 13):
        for c in range(6, 10):
            px(img, r, c, red)

    # Centre highlight stripe col 7
    for r in range(3, 13):
        px(img, r, 7, highlight)

    # Cockpit opening rows 6-8, cols 7-8
    for r in range(6, 9):
        px(img, r, 7, cockpit)
        px(img, r, 8, cockpit)

    # Front wing: rows 1-2, cols 2-13
    for r in range(1, 3):
        for c in range(2, 14):
            px(img, r, c, wing_col)
    # Body overlap on front wing
    for c in range(6, 10):
        px(img, 1, c, red)
        px(img, 2, c, red)

    # Rear wing: rows 13-14, cols 3-12
    for r in range(13, 15):
        for c in range(3, 13):
            px(img, r, c, wing_col)
    for c in range(6, 10):
        px(img, 13, c, red)
        px(img, 14, c, red)

    # Front wheels: 2×3 each
    # Left: cols 2-3, rows 4-6
    for r in range(4, 7):
        px(img, r, 2, black)
        px(img, r, 3, black)
    # Right: cols 12-13, rows 4-6
    for r in range(4, 7):
        px(img, r, 12, black)
        px(img, r, 13, black)

    # Rear wheels: 3×3 each
    # Left: cols 2-4, rows 9-11
    for r in range(9, 12):
        for c in range(2, 5):
            px(img, r, c, black)
    # Right: cols 11-13, rows 9-11
    for r in range(9, 12):
        for c in range(11, 14):
            px(img, r, c, black)

    write_png(OUT_DIR / "prototype_car_spawn.png", img)


# ---------------------------------------------------------------------------
# 9. crude_oil_chunk.png — black tarry ore chunk
# ---------------------------------------------------------------------------

def gen_crude_oil_chunk():
    img = blank()
    BASE  = (15,  15,  15, 255)
    HI    = (60,  70,  75, 255)
    FACET = (30,  30,  32, 255)

    # Irregular jagged shape ~10x10, centered
    shape = [
        (3,  4, 11),
        (4,  3, 12),
        (5,  3, 12),
        (6,  2, 13),
        (7,  3, 12),
        (8,  3, 12),
        (9,  4, 11),
        (10, 5, 10),
        (11, 5, 11),
        (12, 4, 10),
    ]
    for (row, cs, ce) in shape:
        for c in range(cs, ce + 1):
            px(img, row, c, BASE)

    # Top-right slightly lighter facets
    for (r, c) in [(3, 10), (3, 11), (4, 11), (4, 12), (5, 12)]:
        px(img, r, c, FACET)

    # Glossy highlights top-left
    for (r, c) in [(3, 4), (4, 3), (4, 4), (5, 4)]:
        px(img, r, c, HI)

    write_png(OUT_DIR / "crude_oil_chunk.png", img)


# ---------------------------------------------------------------------------
# 10. crude_oil_bucket.png — bucket with black oil
# ---------------------------------------------------------------------------

def gen_crude_oil_bucket():
    img = blank()
    IRON = (150, 150, 150, 255)
    BODY = (130, 130, 130, 255)
    OIL  = (10,  10,  12,  255)
    DARK = (100, 100, 100, 255)

    body_cols = {
        6:  (4, 11), 7:  (4, 11), 8:  (4, 11), 9:  (4, 11),
        10: (4, 11), 11: (4, 11), 12: (4, 11),
        13: (5, 10), 14: (5, 10),
        15: (6,  9),
    }
    for row, (cs, ce) in body_cols.items():
        for c in range(cs, ce + 1):
            color = OIL if row <= 9 else BODY
            px(img, row, c, color)
        px(img, row, cs, DARK)
        px(img, row, ce, DARK)

    # Top rim
    for c in range(4, 12):
        px(img, 6, c, IRON)

    # Bottom base
    for c in range(6, 10):
        px(img, 15, c, IRON)

    # Handle arc rows 3-5, cols 7-9
    for c in range(7, 10):
        px(img, 3, c, IRON)
    px(img, 4, 7, IRON); px(img, 4, 9, IRON)
    px(img, 5, 7, IRON); px(img, 5, 9, IRON)

    write_png(OUT_DIR / "crude_oil_bucket.png", img)


# ---------------------------------------------------------------------------
# 11. gas.png — small silver gas canister
# ---------------------------------------------------------------------------

def gen_gas():
    img = blank()
    SILVER = (160, 160, 165, 255)
    BAND   = (110, 110, 115, 255)
    VALVE  = (90,  90,  95,  255)
    BOTTOM = (140, 140, 145, 255)

    # Cylinder cols 5-10, rows 3-12
    for r in range(3, 13):
        for c in range(5, 11):
            if r in (3, 4):
                px(img, r, c, BAND)
            elif r == 12:
                px(img, r, c, BOTTOM)
            else:
                px(img, r, c, SILVER)

    # Valve nub 2x2 at rows 1-2, cols 7-8
    for r in range(1, 3):
        for c in range(7, 9):
            px(img, r, c, VALVE)

    write_png(OUT_DIR / "gas.png", img)


# ---------------------------------------------------------------------------
# 12. petrol_can.png — red jerrycan
# ---------------------------------------------------------------------------

def gen_petrol_can():
    img = blank()
    RED    = (200,  40,  40, 255)
    SHADOW = (160,  30,  30, 255)
    HI     = (230, 100, 100, 255)
    CAP    = (140, 140, 145, 255)

    # Body cols 3-12, rows 4-14
    for r in range(4, 15):
        for c in range(3, 13):
            if c >= 11:
                px(img, r, c, SHADOW)
            elif c == 3:
                px(img, r, c, HI)
            else:
                px(img, r, c, RED)

    # Cap/nozzle rows 2-3, cols 6-8
    for r in range(2, 4):
        for c in range(6, 9):
            px(img, r, c, CAP)

    write_png(OUT_DIR / "petrol_can.png", img)


# ---------------------------------------------------------------------------
# 13. diesel_can.png — dark green jerrycan
# ---------------------------------------------------------------------------

def gen_diesel_can():
    img = blank()
    GREEN  = (50,  90,  50, 255)
    SHADOW = (35,  65,  35, 255)
    HI     = (80, 130,  80, 255)
    CAP    = (140, 140, 145, 255)

    for r in range(4, 15):
        for c in range(3, 13):
            if c >= 11:
                px(img, r, c, SHADOW)
            elif c == 3:
                px(img, r, c, HI)
            else:
                px(img, r, c, GREEN)

    for r in range(2, 4):
        for c in range(6, 9):
            px(img, r, c, CAP)

    write_png(OUT_DIR / "diesel_can.png", img)


# ---------------------------------------------------------------------------
# 14. crude_rubber.png — sticky brown/black rubber glob
# ---------------------------------------------------------------------------

def gen_crude_rubber():
    img = blank()
    BASE  = (40,  25,  15, 255)
    LIGHT = (70,  45,  25, 255)
    CORE  = (15,  10,   8, 255)

    shape = [
        (3,  4, 11),
        (4,  3, 12),
        (5,  3, 12),
        (6,  3, 12),
        (7,  3, 12),
        (8,  3, 12),
        (9,  4, 11),
        (10, 4, 11),
        (11, 5, 10),
    ]
    for (row, cs, ce) in shape:
        for c in range(cs, ce + 1):
            px(img, row, c, BASE)

    for (r, c) in [(4, 5), (5, 10), (6, 4), (7, 11), (8, 6), (9, 9)]:
        px(img, r, c, LIGHT)

    # Black core 3x3 center
    for r in range(6, 9):
        for c in range(6, 9):
            px(img, r, c, CORE)

    write_png(OUT_DIR / "crude_rubber.png", img)


# ---------------------------------------------------------------------------
# 15. rubber.png — clean black rubber sheet/ingot
# ---------------------------------------------------------------------------

def gen_rubber():
    img = blank()
    BLACK  = (30,  30,  30, 255)
    HI     = (60,  60,  62, 255)
    SHADOW = (15,  15,  15, 255)

    # 10x6 rectangle cols 3-12, rows 5-10
    for r in range(5, 11):
        for c in range(3, 13):
            if r == 5:
                px(img, r, c, HI)
            elif r == 10:
                px(img, r, c, SHADOW)
            else:
                px(img, r, c, BLACK)

    write_png(OUT_DIR / "rubber.png", img)


# ---------------------------------------------------------------------------
# 16. asphalt_binder.png — black tar lump/pellet
# ---------------------------------------------------------------------------

def gen_asphalt_binder():
    img = blank()
    BASE  = (18,  15,  12, 255)
    HI    = (40,  35,  30, 255)
    SHEEN = (70,  65,  60, 255)

    corners = {(4, 4), (4, 11), (11, 4), (11, 11)}
    for r in range(4, 12):
        for c in range(4, 12):
            if (r, c) not in corners:
                px(img, r, c, BASE)

    # Top highlight 3px at row 4, cols 5-7
    for c in range(5, 8):
        px(img, 4, c, HI)

    # Sheen dot row 6, col 5
    px(img, 6, 5, SHEEN)

    write_png(OUT_DIR / "asphalt_binder.png", img)


# ---------------------------------------------------------------------------
# 17. plastic.png — white/light grey plastic sheet
# ---------------------------------------------------------------------------

def gen_plastic():
    img = blank()
    BASE   = (210, 210, 215, 255)
    BORDER = (170, 170, 175, 255)
    HI     = (235, 235, 240, 255)

    # 10x8 rectangle cols 3-12, rows 4-11
    for r in range(4, 12):
        for c in range(3, 13):
            is_border = (r == 4 or r == 11 or c == 3 or c == 12)
            px(img, r, c, BORDER if is_border else BASE)

    # Top-left highlight 2x2 at rows 5-6, cols 4-5
    for r in range(5, 7):
        for c in range(4, 6):
            px(img, r, c, HI)

    write_png(OUT_DIR / "plastic.png", img)


# ---------------------------------------------------------------------------
# 18. racing_electronics.png — circuit board
# ---------------------------------------------------------------------------

def gen_racing_electronics():
    img = blank()
    PCB   = (30,  55,  30, 255)
    TRACE = (180, 150,  20, 255)
    CHIP  = (200, 200, 205, 255)
    LED   = (220,  30,  30, 255)

    # 12x10 PCB rectangle cols 2-13, rows 3-12
    for r in range(3, 13):
        for c in range(2, 14):
            px(img, r, c, PCB)

    # 3 gold trace lines at rows 4, 7, 10
    for trace_row in (4, 7, 10):
        for c in range(2, 14):
            px(img, trace_row, c, TRACE)

    # Chip A: rows 5-6, cols 3-5
    for r in range(5, 7):
        for c in range(3, 6):
            px(img, r, c, CHIP)

    # Chip B: rows 5-6, cols 8-10
    for r in range(5, 7):
        for c in range(8, 11):
            px(img, r, c, CHIP)

    # 4 LED dots at corners of chip area
    for (lr, lc) in [(5, 3), (5, 10), (6, 3), (6, 10)]:
        px(img, lr, lc, LED)

    write_png(OUT_DIR / "racing_electronics.png", img)


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

GENERATORS = [
    ("chassis.png",              gen_chassis),
    ("engine.png",               gen_engine),
    ("gearbox.png",              gen_gearbox),
    ("steering_controls.png",    gen_steering_controls),
    ("tires.png",                gen_tires),
    ("aero_kit.png",             gen_aero_kit),
    ("carbon_fiber.png",         gen_carbon_fiber),
    ("prototype_car_spawn.png",  gen_prototype_car_spawn),
    # Oil/refining/progression items
    ("crude_oil_chunk.png",      gen_crude_oil_chunk),
    ("crude_oil_bucket.png",     gen_crude_oil_bucket),
    ("gas.png",                  gen_gas),
    ("petrol_can.png",           gen_petrol_can),
    ("diesel_can.png",           gen_diesel_can),
    ("crude_rubber.png",         gen_crude_rubber),
    ("rubber.png",               gen_rubber),
    ("asphalt_binder.png",       gen_asphalt_binder),
    ("plastic.png",              gen_plastic),
    ("racing_electronics.png",   gen_racing_electronics),
]

if __name__ == "__main__":
    print(f"Generating {len(GENERATORS)} item textures into {OUT_DIR} ...")
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for name, fn in GENERATORS:
        fn()
    print("All done.")
