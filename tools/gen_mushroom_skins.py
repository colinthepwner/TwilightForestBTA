#!/usr/bin/env python3
"""
Generate the three giant-mushroom skin textures out of BTA's own art.

Idempotent and deterministic: same inputs, same bytes. Writes exactly three files into
src/main/resources/assets/twilightforest/textures/block/:

    mushroom_skin_brown.png   mushroom_skin_red.png
    mushroom_skin_stem.png    mushroom_skin_inside.png

    python tools/gen_mushroom_skins.py [--dry-run] [--bta-dir PATH]

Why this exists
---------------
Twilight Forest 1.7.1's canopy mushroom is built from Minecraft 1.2.3's **huge mushroom blocks**
-- `TFGenCanopyMushroom` writes `ox.bn` (block 99, brown) with metadata 10 for the stalk and
`ox.bo` (block 100, red) or `ox.bn` with metadata 5 for the cap, where 10 and 5 are
`BlockHugeMushroom` FACE values and not colours. **BTA has no giant mushroom block of any kind**
-- `Blocks` registers only the small plants MUSHROOM_BROWN (340) and MUSHROOM_RED (341), there is
no BlockHugeMushroom equivalent, and no feature places one. So the port has to bring its own
block, and a block needs art nobody has: BTA never drew a giant mushroom, and the Twilight
Forest archive does not contain one either (its own `terrain.png` has no mushroom skin -- upstream
was using the vanilla block's).

The asset bridge cannot help here for once. It slices art out of the player's copy of the
original mod, and this art is not in the original mod: it belonged to Minecraft. Bridging it would
mean depending on a Minecraft jar the player has no reason to own -- which is exactly the bug the
sibling BOP port shipped in 0.1.0-0.1.3 and had reported as "the magenta badlands".

Method
------
⚠️ REWRITTEN. The first version recoloured donor textures the way the BOP port's
`gen_vanilla_standins.py` does -- caps from ``mud_baked.png``, stem from ``log/oak_side.png``.
That works for terracotta, which is *supposed* to look like cracked earth, and it looks terrible
on a mushroom: mud_baked's crackle is high-contrast and blobby, so a cap tiled across a
five-block span read as churning noise rather than as a surface. Reported from play, accurately,
as "horrible classic Windows XP screensavers".

So the pattern is now GENERATED rather than borrowed, in the style beta Minecraft's own block art
uses: a flat base colour plus a fine, low-amplitude, per-pixel grain. That is what makes a beta
texture read as a smooth surface at a distance and still have something to look at up close --
the noise is high-FREQUENCY (per pixel, uncorrelated) and low-AMPLITUDE (a few percent), which is
the exact opposite of a donor recolour, where the noise is low-frequency and high-amplitude.

  * Both caps: flat base + ``GRAIN`` amplitude of hash noise. No structure, no blobs.
  * The stem: the same, but the hash is dominated by X so the grain correlates down columns --
    a fibrous vertical stalk without an oak plank's hard stripes.
  * The red cap gets spots, because a red mushroom without spots is a tomato. Their layout is
    SAMPLED rather than listed -- see SPOT_COUNT -- because hand-placed positions read as an
    arrangement once the tile repeats.

The noise is a deterministic integer hash of (x, y), NOT `random` -- same inputs, same bytes,
every run, on every machine.

Colour
------
The three hues are still SAMPLED from BTA's own small mushrooms (``mushroom_brown.png`` /
``mushroom_red.png``) at run time, so a giant mushroom reads as the same species as the small one
growing beside it, and it tracks BTA's palette if Turnip Labs ever retint them.

Provenance
----------
This changes with the rewrite, and in the port's favour. The old outputs were **Adapted Material
of BTA's art** -- Turnip Labs' pixels, recoloured -- which is why they are generated rather than
committed. The new outputs borrow no pixel pattern from anyone: every pixel is computed here, and
the only thing taken from BTA is three MEAN COLOURS, which is a measurement rather than a copy.

They are still generated rather than committed, for now, because the hue sampling needs a BTA jar
at run time. If they were ever wanted in the repository the licensing objection no longer stands --
that is a decision, not a constraint.

Requires Pillow, and an extracted BTA client jar. By default it looks in `tools/bta_jar/`, which
is gitignored; point `--bta-dir` anywhere else if you keep yours elsewhere.
"""
from __future__ import annotations

import argparse
import sys
from pathlib import Path

try:
    from PIL import Image
except ImportError:
    sys.exit("Pillow is required: python -m pip install pillow")

REPO = Path(__file__).resolve().parents[1]
DEFAULT_BTA = REPO / "tools/bta_jar/assets/minecraft/textures/block"
OUT_DIR = REPO / "src/main/resources/assets/twilightforest/textures/block"

# Spots are filled circles, in two tones so they sit in the texture instead of floating on it.
#
# ⚠️ Small discs do not rasterise round. A radius-2 Euclidean disc is thirteen pixels arranged as a
# cross and a taxicab one is a diamond; both were tried and both read as punctuation. The fix is
# SIZE, not a cleverer mask: at diameter 5 and 6 a circle has enough pixels to actually look like
# one, and the corner-knocking that a 3x3 needed becomes the mask's own job.
#
# Measured on the half-pixel grid -- a pixel is inside if its CENTRE is within the radius -- which is
# what makes an even diameter come out symmetrical instead of lopsided.
#
# The spots are LAID OUT BY THE SAMPLER BELOW rather than listed by hand.
#
# ⚠️ Hand-placed spots cannot look random, and adding more of them does not help. Three or four
# positions chosen by eye always read as an arrangement -- the eye is far better at spotting a
# lattice than a person is at avoiding one -- and the failure is worse after tiling, because a cap
# repeats the same handful of positions every 16 pixels.
#
# So the layout is sampled instead: dart-throwing with a minimum separation, on the torus, from the
# same deterministic hash the grain uses.
#
# ⚠️ SPOT_GAP is the dial that matters, and its sign is the whole character of the texture. A large
# positive gap gives blue noise -- evenly spread, no clumps, no holes -- which is mathematically the
# nicest scatter and reads as a POLKA PRINT, because evenness is what the eye calls deliberate. A
# negative gap lets spots overlap into lumpy merged blobs with bare red between them, which is what
# a real cap looks like. Four candidate settings were rendered and compared tiled before this one
# was chosen; the value below is that choice, not a default.
#
# Spots may cross a tile edge and rejoin on the opposite one; the sampler works on the torus, so the
# separation test already measures across the seam and a crossing spot is spaced correctly against
# its neighbours on the far side.
SPOT_COUNT = 4

# Weighted, not uniform: 6 appears twice so the layout leans large. Six same-ish spots read
# as a polka print however irregularly they are placed -- what makes a scatter look natural is
# the SIZE variance as much as the position, and a couple of big caps with smaller ones
# between them is what a real fly agaric has.
SPOT_DIAMETERS = (4, 5, 6, 6)

# Minimum gap between two spots' EDGES, in pixels. Below about 1 the discs start to touch and read as
# a blob; much above 2 and six of them will not fit, and the sampler quietly returns fewer.
SPOT_GAP = 1.0
SPOT_LIGHT = (236, 230, 221)
SPOT_SHADE = (206, 198, 188)

# Grain amplitude, as a fraction of the base colour. This is the whole difference between the beta
# look and the screensaver look, so it is worth being precise about:
#
#   0.00  flat colour -- reads as plastic, and tiles invisibly, which is worse than it sounds
#   0.09  what beta's own dirt/stone sit near: visible up close, gone at five blocks   <-- here
#   0.30+ what a mud_baked recolour produced: churns
GRAIN = 0.09

# The brown cap gets less. Not a preference dressed up as a rule -- the same amplitude is visibly
# noisier on brown than on the other two, and there is a reason: the red cap is saturated, so the
# eye reads its variation as hue rather than as speckle, and the stem is near-white, where a 9%
# swing is only a few levels of headroom. The brown sits in the mid-tones on all three channels at
# once, which is exactly where luminance noise is most legible. It also covers the largest
# uninterrupted area of any of the three -- a brown cap has no spots to break it up.
GRAIN_BROWN = 0.05

# The stem's grain leans vertical: this is how much of the hash comes from Y rather than X. Low
# means "the value barely changes going down a column", i.e. fibres.
STEM_Y_WEIGHT = 0.25

# How far the inside is mixed towards white FROM THE BROWN CAP'S HUE. Not from the stalk's, which
# BTA's small mushrooms sample at a near-neutral (218, 218, 218) -- lightening that gives a paler
# grey, and a grey underside is the thing being fixed. Mushroom flesh is warm, so the cap's own hue
# diluted is the honest source for it, and 0.65 lands on a cream that is clearly not the stem.
INSIDE_LIGHTEN = 0.65

# The inside's grain. HIGHER than the others and deliberately so: this is the one surface that is
# supposed to look porous rather than smooth, and the pores are what a player sees when they stand
# under a cap. At GRAIN it reads as flat cream.
GRAIN_INSIDE = 0.13


def lighten(rgb, fraction: float):
    """Mix a colour towards white. Used for the inside, which is paler than the skin around it."""
    return tuple(int(round(c + (255 - c) * fraction)) for c in rgb)


def lum(p) -> float:
    return 0.299 * p[0] + 0.587 * p[1] + 0.114 * p[2]


def load(base: Path, name: str) -> Image.Image:
    path = base / name
    if not path.is_file():
        sys.exit(f"missing BTA texture: {path}\n"
                 "Point --bta-dir at an extracted BTA client jar's textures/block directory.")
    return Image.open(path).convert("RGBA")


def mean_lum(im: Image.Image) -> float:
    px = im.load()
    values = [lum(px[x, y]) for y in range(im.height) for x in range(im.width) if px[x, y][3] > 0]
    return sum(values) / len(values) if values else 1.0


def hash_noise(x: int, y: int, seed: int) -> float:
    """Deterministic per-pixel value in [0, 1). No `random`: same inputs, same bytes, forever.

    An integer avalanche hash rather than a smooth/Perlin noise on purpose. Smooth noise produces
    exactly the low-frequency blobbing this rewrite exists to get rid of; what a beta texture has is
    per-pixel hash, uncorrelated with its neighbours.
    """
    h = (x * 73856093) ^ (y * 19349663) ^ (seed * 83492791)
    h &= 0xFFFFFFFF
    h ^= h >> 13
    h = (h * 1274126177) & 0xFFFFFFFF
    h ^= h >> 16
    return (h & 0xFFFF) / 65536.0


def grained(rgb, seed: int, size: int = 16, y_weight: float = 1.0,
            amplitude: float = GRAIN) -> Image.Image:
    """A flat `rgb` tile with beta-style grain over it.

    `y_weight` below 1 makes the noise correlate down columns, which is what turns a flat colour
    into a fibrous stalk without drawing stripes.
    """
    out = Image.new("RGBA", (size, size))
    px = out.load()
    for y in range(size):
        for x in range(size):
            # ⚠️ BLENDED, not quantised. Quantising Y (`hash(x, y // 4)`) does correlate columns, and
            # it also makes every fourth row jump in unison -- which renders as horizontal brickwork,
            # the exact artefact this is meant to avoid. Blending a per-column hash with a per-pixel
            # one gives the column a base shade and lets each pixel wander slightly off it, with no
            # banding at any period.
            column = hash_noise(x, 0, seed)
            pixel = hash_noise(x, y, seed)
            n = column * (1.0 - y_weight) + pixel * y_weight
            scale = 1.0 + (n - 0.5) * 2.0 * amplitude
            px[x, y] = tuple(max(0, min(255, int(round(c * scale)))) for c in rgb) + (255,)
    return out


def sampled_colour(im: Image.Image, keep) -> tuple[int, int, int]:
    """Mean colour of the opaque pixels `keep` accepts -- how the cap and stalk hues are read."""
    px = im.load()
    picked = [px[x, y] for y in range(im.height) for x in range(im.width)
              if px[x, y][3] > 0 and keep(px[x, y])]
    if not picked:
        sys.exit("colour sample matched no pixels -- the BTA texture changed shape; retune `keep`")
    n = len(picked)
    return tuple(round(sum(p[i] for p in picked) / n) for i in range(3))


def torus_gap(ax: int, ay: int, ar: float, bx: int, by: int, br: float, size: int) -> float:
    """Edge-to-edge distance between two discs on a wrapping tile.

    Wrapping is what makes the separation test agree with what is actually seen: two spots four
    pixels apart across the seam are four pixels apart on the cap, however far apart their
    coordinates look.
    """
    dx = abs(ax - bx)
    dy = abs(ay - by)
    dx = min(dx, size - dx)
    dy = min(dy, size - dy)
    return (dx * dx + dy * dy) ** 0.5 - (ar + br)


def spot_layout(seed: int, count: int = SPOT_COUNT, size: int = 16,
                gap: float = SPOT_GAP, tries: int = 20000):
    """Blue-noise spot placement: dart-throwing with a minimum separation, deterministic.

    Candidates come from the same hash as the grain, so the layout is fixed for a given seed -- the
    same bytes every run, on every machine, with no `random` anywhere.

    Returns fewer than `count` spots if the tile genuinely cannot hold them rather than looping
    forever; the caller reports what it got, so a too-tight gap shows up as a number instead of a
    hang.
    """
    spots: list[tuple[int, int, int]] = []
    for i in range(tries):
        if len(spots) == count:
            break
        x = int(hash_noise(i, 0, seed) * size) % size
        y = int(hash_noise(0, i, seed + 7919) * size) % size
        d = SPOT_DIAMETERS[int(hash_noise(i, i, seed + 104729) * len(SPOT_DIAMETERS))]

        if all(torus_gap(x, y, d / 2.0, sx, sy, sd / 2.0, size) >= gap
               for sx, sy, sd in spots):
            spots.append((x, y, d))
    return spots


def spotted(cap: Image.Image, spots) -> Image.Image:
    """The red cap plus its spots: round blobs, lit core and a one-pixel shaded rim.

    Wraps at the tile edge on purpose -- a spot clipped at x=15 would reappear as a half-spot
    against the next tile's edge and give the repeat away.
    """
    out = cap.copy()
    px = out.load()
    for cx, cy, diameter in spots:
        r = diameter / 2.0
        span = int(r) + 1
        for dy in range(-span, span + 1):
            for dx in range(-span, span + 1):
                # Pixel CENTRES against the circle: for an even diameter the centre falls on a pixel
                # boundary, so the +0.5 is what keeps the disc symmetrical rather than shifted.
                fx = dx + 0.5 if diameter % 2 == 0 else float(dx)
                fy = dy + 0.5 if diameter % 2 == 0 else float(dy)
                d2 = fx * fx + fy * fy
                if d2 > r * r:
                    continue
                x, y = (cx + dx) % out.width, (cy + dy) % out.height
                # The outer ring takes the darker tone, so the spot has an edge instead of being a
                # flat sticker. Measured against (r - 1) so the rim is one pixel thick at any size.
                rim = d2 > (r - 1.0) * (r - 1.0)
                px[x, y] = (SPOT_SHADE if rim else SPOT_LIGHT) + (255,)
    return out


def main() -> int:
    ap = argparse.ArgumentParser(description="Generate giant-mushroom skins from BTA's art.")
    ap.add_argument("--bta-dir", type=Path, default=DEFAULT_BTA)
    ap.add_argument("--dry-run", action="store_true")
    args = ap.parse_args()
    base = args.bta_dir

    small_brown = load(base, "mushroom_brown.png")
    small_red = load(base, "mushroom_red.png")

    # The small red mushroom's cap = its clearly-red pixels; its stalk (shared look with the brown
    # one's) = its pale pixels. The brown cap filter only has to exclude that pale stalk.
    cap_brown = sampled_colour(small_brown, lambda p: not (p[0] > 180 and p[1] > 170 and p[2] > 150))
    cap_red = sampled_colour(small_red, lambda p: p[0] > p[1] + 40)
    stalk = sampled_colour(small_red, lambda p: p[0] > 180 and p[1] > 170 and p[2] > 150)

    spots = spot_layout(seed=4)

    # Distinct seeds so the three tiles do not share a grain pattern -- two surfaces meeting at a
    # cap's rim with identical noise reads as one continuous block rather than two faces.
    # ⚠️ The inside is NOT the stem texture, and this was got wrong once. Minecraft ships FOUR
    # huge-mushroom files -- skin_brown, skin_red, skin_stem and mushroom_block_inside -- and the
    # last two are different surfaces: a stem has vertical fibres running down it, and the cut
    # underside of a cap has pores and no direction at all. Reusing the stem for both gives every
    # cap a streaky underside, which is what it looked like and what was reported.
    #
    # So: a diluted cap hue, isotropic grain (y_weight left at its default 1.0, where the stem's is
    # 0.25), and a coarser amplitude so it reads as pores.
    #
    # ⚠️ ONE inside for BOTH species, from the BROWN cap, which is vanilla's structure rather than a
    # shortcut -- Minecraft ships a single mushroom_block_inside that a red cap and a brown cap both
    # wear. The flesh of a mushroom is the same stuff whatever colour its skin is, and the brown cap
    # is the one whose hue is already close to flesh. Deriving a red inside from the red cap would
    # give a pink underside, which no mushroom has.
    inside = lighten(cap_brown, INSIDE_LIGHTEN)

    outputs = {
        "mushroom_skin_brown.png": grained(cap_brown, seed=1, amplitude=GRAIN_BROWN),
        "mushroom_skin_red.png": spotted(grained(cap_red, seed=2), spots),
        "mushroom_skin_stem.png": grained(stalk, seed=3, y_weight=STEM_Y_WEIGHT),
        "mushroom_skin_inside.png": grained(inside, seed=5, amplitude=GRAIN_INSIDE),
    }
    print(f"sampled hues: brown cap {cap_brown}, red cap {cap_red}, stalk {stalk}, "
          f"inside {inside}")
    print(f"spot layout: {len(spots)} of {SPOT_COUNT} placed -> {spots}")
    for name, image in outputs.items():
        target = OUT_DIR / name
        if args.dry_run:
            print(f"would write {target.relative_to(REPO)}")
            continue
        target.parent.mkdir(parents=True, exist_ok=True)
        image.save(target)
        print(f"wrote {target.relative_to(REPO)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
