"""Find trees that are not standing on anything.

A tree is floating when the *whole tree* touches no ground, not when some individual log has air
under it. Those are different questions, and asking the second one is what made this tool report a
bug that was not there -- see "What this used to measure" below.

Three outcomes are separated, because they have different causes:

  * a tree rooted on GROUND          -- correct
  * a tree rooted on WATER only      -- planted on a lake surface
  * a tree touching neither          -- planted above the ground it was meant to root in

    python tools/tree_check.py run/tfverify --dim 0

⚠️ WHAT THIS USED TO MEASURE, AND WHY IT WAS WRONG

The previous version called any vertical run of >= 3 logs a "trunk" and then looked at the single
block underneath it. On a measured 279-chunk world that reported 8 floating trunks (2.5%), all
LOG_TWILIGHT_OAK, 3-4 tall, at repeatable coordinates -- which read exactly like a placement bug and
was chased as one.

Every one of those eight was a limb of a *fancy* tree. `WorldFeatureTreeFancy` (which
`TFBiome.getTreeFeature` returns 1 time in 10, with Twilight oak logs) draws its branches with a
diagonal Bresenham line. A diagonal line of logs contains vertical runs: three or four logs stacked
before the line steps sideways. The lowest log of such a run has AIR beneath it and its parent
branch DIAGONALLY beside it -- so a straight-down test says "floating" and a diagonal test says
"attached". Raising MIN_TRUNK does not help; the runs are the same length as a real sapling trunk.

The same run, re-measured by cluster: 307 log clusters, 306 rooted in ground, 1 unrooted -- and that
one is at x=-81, a chunk-boundary column whose trunk lives in a chunk the region files do not
contain. Zero real floating trees. The generator was never wrong; the ruler was.

So this version:

  * loads the whole scanned area into one map instead of working chunk by chunk, because a tree
    straddles chunk borders and a per-chunk view cuts trunks off from their own roots;
  * flood-fills each log cluster with 26-connectivity (diagonals included), which is what actually
    holds a fancy tree together;
  * asks whether ANY log in the cluster sits on ground;
  * reports clusters that touch the edge of the scanned area separately rather than counting them
    as failures, since their missing part is missing from the *files*, not from the world.
"""

import argparse
import collections
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from scan_world import level_of, region_chunks  # noqa: E402

# Twilight Forest's three logs, then the vanilla ones a Twilight biome can plant (birch and pine
# come out of getTreeFeature just as often as the mod's own oak).
TF_LOGS = {2301, 2302, 2303}
# BTA's own logs, id-checked against Blocks.class in the deobfuscated 8.0.1 jar: oak 280, pine 281,
# birch 282, cherry 283, eucalyptus 284, mossy oak 285, thorn 286, palm 287. Birch and pine come out
# of getTreeFeature as often as the mod's own oak, so leaving them out undercounts the population
# this tool is supposed to be checking.
VANILLA_LOGS = {280, 281, 282, 283, 284, 285, 286, 287}
LOGS = TF_LOGS | VANILLA_LOGS

AIR = 0

# ⚠️ BTA ids, not b1.7.3's. BTA renumbered every block: grass is 200, dirt 220, stone 1, gravel 251,
# bedrock 260. The old list here (1, 2, 3, 12, 13, 60, 110) was vanilla-numbered, so "grass" matched
# stone and "dirt" matched nothing -- the footing histogram was labelling ids it had never looked up.
#
# The set that matters is BTA's GROWS_TREES tag, read off Blocks.class in the deobfuscated 8.0.1 jar,
# because that is the exact test WorldFeatureTree.place applies to the block under a sapling:
#     grass, grass_retro, grass_scorched, dirt, dirt_scorched, mud, mud_baked
# Sand is deliberately NOT in it -- BTA will not grow a tree on sand, so a tree standing on sand
# would itself be a finding.
GRASS, DIRT = 200, 220
GROWS_TREES = {GRASS, 201, 202, DIRT, 221, 225, 226}
# Not part of GROWS_TREES, but legitimate footing all the same: stone and permafrost are what a
# hollow hill's shell or the glacier's surface leaves under a tree, and sand shows up under the
# mangroves the swamp places directly rather than through the sapling path.
OTHER_FOOTING = {1, 7, 250, 730, 731, 740}
GROUND = GROWS_TREES | OTHER_FOOTING

WATER = {270, 271}


def blocks_of(level):
    """Returns {(x, y, z): id} for one chunk, from BTA's sectioned format."""
    out = {}
    for section in level.get("Sections") or []:
        base = section.get("yPos", 0) * 16
        data = section.get("Blocks")
        if not data:
            continue
        for i, value in enumerate(data):
            if value == AIR:
                continue
            y = base + (i >> 8)
            z = (i >> 4) & 15
            x = i & 15
            # 0xFFFF, not 0xFF. BTA stores block ids as a short array and this mod's own blocks
            # are 2301-2313, so masking to a byte turns every one of them into an unrelated
            # vanilla id -- and the measurement reads as a clean result rather than a broken one.
            out[(x, y, z)] = value & 0xFFFF
    return out


def load(region_dir):
    """Whole scanned area as {(x, y, z): id}, plus the set of chunk coordinates present."""
    world = {}
    chunks = set()
    for region in sorted(region_dir.glob("*.mcr")):
        for chunk in region_chunks(region):
            level = level_of(chunk)
            cx, cz = level.get("xPos", 0), level.get("zPos", 0)
            chunks.add((cx, cz))
            for (x, y, z), block in blocks_of(level).items():
                world[(cx * 16 + x, y, cz * 16 + z)] = block
    return world, chunks


def clusters_of(world):
    """Every maximal 26-connected group of logs.

    26-connectivity, not 6: a fancy tree's branch is a diagonal line, so its logs touch only at
    corners. Using face-connectivity here splits one tree into a dozen "trees", most of them
    airborne, which is precisely the false positive this tool used to report.
    """
    logs = {p for p, block in world.items() if block in LOGS}
    seen = set()
    offsets = [(dx, dy, dz)
               for dx in (-1, 0, 1) for dy in (-1, 0, 1) for dz in (-1, 0, 1)
               if (dx, dy, dz) != (0, 0, 0)]

    for start in logs:
        if start in seen:
            continue
        seen.add(start)
        group = [start]
        stack = [start]
        while stack:
            x, y, z = stack.pop()
            for dx, dy, dz in offsets:
                nxt = (x + dx, y + dy, z + dz)
                if nxt in logs and nxt not in seen:
                    seen.add(nxt)
                    group.append(nxt)
                    stack.append(nxt)
        yield group


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("world")
    ap.add_argument("--dim", type=int, default=0)
    ap.add_argument("--show", type=int, default=8, help="how many offending positions to print")
    ap.add_argument("--min-size", type=int, default=3,
                    help="ignore log groups smaller than this (a single decorative log is not a tree)")
    args = ap.parse_args()

    region_dir = Path(args.world) / "dimensions" / str(args.dim) / "region"
    if not region_dir.is_dir():
        sys.exit("no region directory at %s" % region_dir)

    world, chunks = load(region_dir)

    trees = 0
    rooted = 0
    on_water = []
    floating = []
    clipped = []
    footing = collections.Counter()

    for group in clusters_of(world):
        if len(group) < args.min_size:
            continue
        trees += 1

        contacts = collections.Counter()
        for x, y, z in group:
            below = world.get((x, y - 1, z), AIR)
            if below not in LOGS:
                contacts[below] += 1

        ground_hits = sum(n for block, n in contacts.items() if block in GROUND)
        water_hits = sum(n for block, n in contacts.items() if block in WATER)

        # Attribute the tree once, by its best contact, so the histogram counts trees and not logs.
        if ground_hits:
            rooted += 1
            for block, n in contacts.items():
                if block in GROUND:
                    footing[block] += n
            continue

        lowest = min(group, key=lambda p: p[1])
        if water_hits:
            on_water.append(lowest)
            continue

        # A group that reaches the edge of what was scanned is not evidence of anything: the rest of
        # it, roots included, is in a chunk these region files do not contain.
        if any((nx >> 4, nz >> 4) not in chunks
               for x, _, z in group
               for nx, nz in ((x - 1, z), (x + 1, z), (x, z - 1), (x, z + 1))):
            clipped.append(lowest)
            continue

        floating.append(lowest)

    print("%d chunks, %d trees (log groups of >= %d)\n" % (len(chunks), trees, args.min_size))
    if not trees:
        print("NO TREES AT ALL -- that is its own failure.")
        return 1

    print("what rooted trees are standing on (counting contact points):")
    for block, n in footing.most_common(10):
        print("  %-8s %6d" % (block, n))

    print()
    print("rooted:              %d  (%.1f%%)" % (rooted, 100.0 * rooted / trees))
    print("floating (on air):   %d  (%.1f%%)" % (len(floating), 100.0 * len(floating) / trees))
    print("on water:            %d  (%.1f%%)" % (len(on_water), 100.0 * len(on_water) / trees))
    print("clipped by the scanned area (not a failure): %d" % len(clipped))

    for label, hits in (("floating", floating), ("on water", on_water)):
        for x, y, z in hits[: args.show]:
            print("  %s: lowest log at x=%d y=%d z=%d" % (label, x, y, z))

    return 1 if (floating or on_water) else 0


if __name__ == "__main__":
    raise SystemExit(main())
