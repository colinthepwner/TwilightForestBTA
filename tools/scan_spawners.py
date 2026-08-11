"""Tally mob-spawner entity ids in a generated world.

Answers one question: do the spawners the Twilight Forest's structures place name entities the
game can actually build? A spawner with an unknown id is not an error at load time -- BTA stores
the string verbatim and leaves the block inert -- so the only way to catch a typo or a mob that
does not exist in BTA is to read the ids back out of the region files and check them against the
mod's own registration list.

Reuses scan_world.py's NBT reader rather than reimplementing it; that one already handles BTA's
four McRegion deviations.

    python tools/scan_spawners.py run/tfverify --dim 0
"""

import argparse
import sys
from collections import Counter
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from scan_world import level_of, region_chunks  # noqa: E402

# What BTA 8.0.1 can actually spawn, plus what this mod registers. Anything outside this set is a
# dead spawner. Vanilla ids come from EntityDispatcher's registrations; the legacy MCP-style keys
# ("Zombie") are accepted by setMobId but converted on the way in, so only namespaced forms should
# ever appear on disk.
VANILLA = {
    "minecraft:" + name
    for name in (
        "chicken cow pig sheep squid wolf deer butterfly firefly_cluster scorpion "
        "zombie zombie_armored zombie_pigman creeper skeleton spider slime ghast giant "
        "snowman human"
    ).split()
}

MODDED = {
    "twilightforest:" + name
    for name in (
        "wildboar bighorn wilddeer penguin swarmspider hedgespider "
        "redcap skeletondruid wraith hostilewolf"
    ).split()
}

KNOWN = VANILLA | MODDED


def spawner_ids(chunk):
    """Yields the EntityId of every mob-spawner tile entity in one chunk.

    Note the tile-entity id is BTA's namespaced "minecraft:mob_spawner", not 1.2.3's bare
    "MobSpawner" -- the payload key inside it is still "EntityId".
    """
    level = level_of(chunk)
    for tile in level.get("TileEntities", []) or []:
        if tile.get("id") == "minecraft:mob_spawner":
            yield tile.get("EntityId", "<missing>")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("world")
    ap.add_argument("--dim", type=int, default=0)
    args = ap.parse_args()

    region_dir = Path(args.world) / "dimensions" / str(args.dim) / "region"
    if not region_dir.is_dir():
        sys.exit(f"no region directory at {region_dir}")

    counts = Counter()
    chunks = 0
    for region in sorted(region_dir.glob("*.mcr")):
        for chunk in region_chunks(region):
            chunks += 1
            counts.update(spawner_ids(chunk))

    total = sum(counts.values())
    print(f"{chunks} chunks, {total} spawners\n")

    if not total:
        print("NO SPAWNERS FOUND -- structures did not place any, which is its own failure.")
        return 1

    width = max(len(k) for k in counts)
    unknown = 0
    for mob_id, n in counts.most_common():
        ok = mob_id in KNOWN
        unknown += 0 if ok else n
        print(f"  {mob_id:<{width}}  {n:5d}  {'ok' if ok else 'UNKNOWN -- dead spawner'}")

    print()
    if unknown:
        print(f"FAIL: {unknown} spawners name an entity nothing can build.")
        return 1

    print("PASS: every spawner names a registered entity.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
