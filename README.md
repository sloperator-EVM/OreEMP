# OreEMP

OreEMP is a Fabric client mod that simulates ore placement from a known seed and renders predicted ore blocks with in-world boxes.

## Commands

- `/oreemp seed <long>` sets the world seed used by simulation.
- `/oreemp start` scans loaded chunks, begins chunk-load scanning, and renders ores.
- `/oreemp stop` clears all simulated data and stops rendering.

## Build

1. Enter the nix shell:
   ```bash
   nix-shell
   ```
2. Bootstrap the wrapper jar locally:
   ```bash
   tmpdir="$(mktemp -d)"
   cat > "$tmpdir/settings.gradle" <<'SG'
   rootProject.name = 'wrapper-bootstrap'
   SG
   cat > "$tmpdir/build.gradle" <<'BG'
   BG
   gradle -p "$tmpdir" wrapper --gradle-version 8.10.2
   cp "$tmpdir/gradle/wrapper/gradle-wrapper.jar" ./gradle/wrapper/
   rm -rf "$tmpdir"
   ```
3. Build the mod jar:
   ```bash
   ./gradlew build
   ```
4. Output jar will be in `build/libs`.
