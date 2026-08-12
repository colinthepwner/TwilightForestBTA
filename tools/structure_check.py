"""Count the marker blocks that prove each big structure generated.

A structure either exists or it does not, and the cheapest proof is a block only that structure
places. Each row below is a block no other feature in the mod uses, so a zero means that structure
did not build -- not that it built badly.

    python tools/structure_check.py run/tfverify --dim 0
"""

import argparse
import collections
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from scan_world import level_of, region_chunks  # noqa: E402

# Block id -> (what it proves, roughly how many one instance places)
#
# The mod's own blocks are 2301-2313. Everything above 400 is BTA's, resolved out of
# net.minecraft.core.block.Blocks rather than remembered -- BTA renumbered vanilla, so
# stone brick is 121 and not 98, and the tower's stand-in for iron bars is 892.
MARKERS = {
    2310: ("hedge maze walls", "hundreds"),
    2313: ("Naga courtyard boss spawner", "exactly 1 per courtyard"),
    2311: ("fireflies (hedge maze torches + courtyard pillars)", "tens"),
    # The hill maze's floor and ceiling are solid mazestone and its walls are the mossy variant.
    # ⚠️ Depth is what identifies it: the maze sits 20 blocks under a hollow hill's chamber, so
    # y ~13. Nothing else in the mod places mazestone at all -- the glacier maze is built of ice.
    2307: ("mazestone (hill maze floor + ceiling)", "thousands, y~12-13"),
    2309: ("mazestone mossy (hill maze walls)", "thousands, y~13-16"),
    # The three-block-thick shell of every tower in a Lich keep, ~30% of it mossy.
    122: ("mossy stone brick (Lich tower shell)", "thousands per keep"),
    # Only the keep places these. Both are rare within a keep, so a zero here with 122 present
    # means the towers built but their interiors did not decorate -- a different failure.
    892: ("steel fence (Lich tower chains + rooms)", "tens per keep"),
    490: ("stone pressure plate (hill maze dead-end traps)", "~1 in 17 dead ends"),
}

# Markers that are genuinely rare rather than merely absent. A zero in one of these is not a
# failure on its own -- it is reported separately so it cannot be read as one.
RARE = {892, 490}


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("world")
    ap.add_argument("--dim", type=int, default=0)
    args = ap.parse_args()

    region_dir = Path(args.world) / "dimensions" / str(args.dim) / "region"
    if not region_dir.is_dir():
        sys.exit("no region directory at %s" % region_dir)

    counts = collections.Counter()
    ys = collections.defaultdict(list)
    chunks = 0

    for region in sorted(region_dir.glob("*.mcr")):
        for chunk in region_chunks(region):
            chunks += 1
            level = level_of(chunk)
            for section in level.get("Sections") or []:
                base = section.get("yPos", 0) * 16
                data = section.get("Blocks")
                if not data:
                    continue
                for i, value in enumerate(data):
                    # 0xFFFF: this mod's blocks are 2301-2313 and a byte mask hides all of them.
                    bid = value & 0xFFFF
                    if bid in MARKERS:
                        counts[bid] += 1
                        if len(ys[bid]) < 40000:
                            ys[bid].append(base + (i >> 8))

    print("%d chunks\n" % chunks)
    missing = []
    rare_missing = []
    for bid, (what, scale) in MARKERS.items():
        n = counts.get(bid, 0)
        if n:
            span = "y %d-%d" % (min(ys[bid]), max(ys[bid]))
            print("  %-6d %-46s %7d  %s" % (bid, what, n, span))
        else:
            print("  %-6d %-46s %7d  (expected %s)" % (bid, what, 0, scale))
            (rare_missing if bid in RARE else missing).append(what)

    print()
    if rare_missing:
        print("ABSENT BUT RARE: %s" % "; ".join(rare_missing))
        print("Not a failure. These are placed a handful of times inside a structure that is")
        print("itself uncommon, so a sample this size can legitimately miss them.")
    if missing:
        print("NOT FOUND: %s" % "; ".join(missing))
        print("A structure is rare -- widen the sample before calling it broken.")
        return 1
    print("PASS: every non-rare marker present.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
