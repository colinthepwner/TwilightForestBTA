# Twilight Forest for Better than Adventure

Benimatic's **Twilight Forest** ported to **BTA 8.0.1**.

A dimension of permanent dusk. Fourteen biomes under a sky that never moves — dense canopy forest,
highlands, glaciers, mushroom groves and mangrove swamps — with hollow hills, mazes, towers and the
things that live in them.

Work in progress. No release yet.

## Requires your own copy of the original mod

**This mod ships none of the original's art.** The textures and models belong to Benimatic, not to
this port, so they are not in the repository and not in any build.

Drop your own copy of the original anywhere under your game directory and it will be found and used
automatically. It does not have to be in `mods/`, does not have to keep its name, and does not have
to be zipped — an unpacked folder works too. Nothing is downloaded; the file has to already be on
your disk.

Without it the mod still runs. Blocks fall back to a vanilla look and mobs render untextured.

## Building

```bash
./gradlew build
```

Java 17 toolchain, Gradle wrapper included. The jar lands in `build/libs/`.

`check` runs a guard that fails the build if any of the original's art has found its way into the
tree.

## Requirements

- Better than Adventure 8.0.1
- HalpLibe 6.1.4+8.0

## Credits

Twilight Forest is by **Benimatic**. This is an unaffiliated port. The port's own Java is this
repository's; the original mod's art is not, and is read from your copy rather than redistributed.
