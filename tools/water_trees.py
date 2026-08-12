"""How many trees actually stand in water, and which species.

tree_check.py asks whether a tree is ROOTED. This asks a different question -- whether the tree is
standing in water even though it is properly rooted -- because a tree planted on submerged ground
looks wrong to a player while passing every rooting test there is.

A tree counts as "in water" when any log in its cluster has water as a direct face neighbour.
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from scan_world import level_of, region_chunks  # noqa: E402

TF_LOGS = {2301: "twilight oak", 2302: "canopy", 2303: "mangrove"}
VANILLA_LOGS = {280: "oak", 281: "pine", 282: "birch", 283: "cherry",
                284: "eucalyptus", 285: "mossy oak", 286: "thorn", 287: "palm"}
LOGS = {**TF_LOGS, **VANILLA_LOGS}
WATER = {270, 271}
SEA = 32

region_dir = Path(sys.argv[1]) / "dimensions" / "0" / "region"
world = {}
for region in sorted(region_dir.glob("*.mcr")):
    for chunk in region_chunks(region):
        level = level_of(chunk)
        cx, cz = level.get("xPos", 0), level.get("zPos", 0)
        for section in level.get("Sections") or []:
            base = section.get("yPos", 0) * 16
            data = section.get("Blocks")
            if not data:
                continue
            for i, value in enumerate(data):
                bid = value & 0xFFFF
                if bid == 0:
                    continue
                world[(cx * 16 + (i & 15), base + (i >> 8), cz * 16 + ((i >> 4) & 15))] = bid

logs = {p: b for p, b in world.items() if b in LOGS}
seen, in_water, total = set(), {}, {}
offsets = [(dx, dy, dz) for dx in (-1, 0, 1) for dy in (-1, 0, 1) for dz in (-1, 0, 1)
           if (dx, dy, dz) != (0, 0, 0)]
faces = [(1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)]

for start in logs:
    if start in seen:
        continue
    seen.add(start)
    group, stack = [start], [start]
    while stack:
        x, y, z = stack.pop()
        for dx, dy, dz in offsets:
            n = (x + dx, y + dy, z + dz)
            if n in logs and n not in seen:
                seen.add(n)
                group.append(n)
                stack.append(n)
    species = LOGS[logs[min(group, key=lambda p: p[1])]]
    total[species] = total.get(species, 0) + 1
    wet = any(world.get((x + dx, y + dy, z + dz)) in WATER
              for (x, y, z) in group for dx, dy, dz in faces)
    if wet:
        in_water[species] = in_water.get(species, 0) + 1

print("trees touching water, by species (trunk species = lowest log):\n")
print("%-14s %8s %8s %8s" % ("species", "total", "in water", "pct"))
for species in sorted(total, key=lambda s: -total[s]):
    n, w = total[species], in_water.get(species, 0)
    print("%-14s %8d %8d %7.1f%%" % (species, n, w, 100.0 * w / n))
print("\nTOTAL %d trees, %d touching water (%.1f%%)"
      % (sum(total.values()), sum(in_water.values()),
         100.0 * sum(in_water.values()) / max(1, sum(total.values()))))
