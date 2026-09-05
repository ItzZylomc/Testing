# Hotbar Mod (Fabric, MC 1.21.11, Java 21)

A client-side Fabric mod that shrinks your hotbar. Type `/hotbarmod` in
chat (works in singleplayer and on any server, since it only touches
your own screen) to open a settings screen with:

- **Hotbar Scale** slider (40%–100%)
- **Vertical Offset** slider (nudge it up/down)
- **Scale Offhand/Attack Icons** toggle
- **Reset to Default** button

Settings are saved to `config/hotbarmod.json` and reload automatically
next time you launch the game.

## Requirements

- **Java 21** (this project's Gradle toolchain targets 21, matching
  what Minecraft 1.21.11 itself requires)
- [Fabric Loader](https://fabricmc.net/use/) 0.18.5+ installed in your
  Minecraft launcher
- [Fabric API](https://modrinth.com/mod/fabric-api) for **1.21.11**
  dropped in your `mods` folder alongside the built jar

## Before you build: verify the version numbers

Fabric's Loader/API/Yarn builds update constantly, faster than any
static guide can track. Before running Gradle:

1. Go to **https://fabricmc.net/develop**
2. Select Minecraft version **1.21.11**
3. Copy the exact **Yarn Mappings**, **Fabric Loader**, and **Fabric
   API** version strings shown there
4. Paste them into `gradle.properties` (`yarn_mappings`,
   `loader_version`, `fabric_version`)

The values already in `gradle.properties` are the latest ones I could
confirm at the time this was written, but please double-check — a
mismatched build number is the #1 reason a fresh Fabric project fails
to sync.

## Building

```bash
# from the project root
./gradlew build
```

(If you don't have a `gradlew` wrapper yet because you're not opening
this in an IDE with Gradle support, run `gradle wrapper` once first,
using any locally installed Gradle 8.x+.)

The built mod jar appears in `build/libs/hotbarmod-1.0.0.jar`. Drop
that into your `.minecraft/mods` folder next to Fabric API.

## Running/testing while developing

```bash
./gradlew runClient
```

This launches a dev instance of Minecraft with the mod already loaded,
so you can test `/hotbarmod` immediately without manually installing
anything.

## If the build fails on the mixin (`HotbarScaleMixin`)

The hotbar-shrinking effect works by injecting into
`InGameHud#renderHotbar` and wrapping it in a scale transform. Method
names and the exact matrix-stack API on `DrawContext` have shifted a
few times across 1.20.x–1.21.x as Mojang/Fabric refactored rendering
code. If you get a mixin application error mentioning
`renderHotbar` when launching:

1. Run `./gradlew genSources` (or use your IDE's "Generate Sources"
   action) so you have readable, decompiled Minecraft source to look at.
2. Open `net.minecraft.client.gui.hud.InGameHud` and find the real
   hotbar-rendering method's name and parameter types for your exact
   build.
3. Update the `method = "..."` strings in
   `src/main/java/com/hotbarmod/mixin/HotbarScaleMixin.java` to match.
4. If `context.getMatrices()` doesn't expose `pushMatrix()` /
   `popMatrix()` / `translate()` / `scale()` on your build, check what
   it does expose (older builds used a `MatrixStack` with `push()`/
   `pop()` instead) and adjust those four calls accordingly.

This kind of small adjustment is normal Fabric modding maintenance —
mappings for the exact rendering internals change slightly release to
release, while everything else in this project (the command, the
config screen, the config file) is stable, ordinary Fabric API and
won't need touching.

## Project layout

```
src/main/java/com/hotbarmod/
├── HotbarMod.java           - client entrypoint, registers /hotbarmod
├── HotbarConfig.java        - settings + JSON load/save
├── HotbarConfigScreen.java  - the GUI that pops up
└── mixin/
    └── HotbarScaleMixin.java - actually shrinks the hotbar render
```
