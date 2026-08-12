#!/usr/bin/env bash
# Generate one Twilight Forest census world for a given seed and census it.
#
# The world type CANNOT be set from server.properties -- neither level-type nor world-type is read
# for this. The only lever is the WorldType string inside dimensions/0/dimension.dat, and the server
# REWRITES that file on shutdown, so the patch has to land while no server is running.
#
# Hence two server runs per seed: one to create the world, one to generate terrain with the patched
# type. Between them the region dir is deleted so nothing vanilla survives.
#
# ⚠️ A leftover server JVM from an earlier run keeps an OS-level lock on the region files, so the
# world cannot be deleted and this script aborts rather than censusing a stale world. That is why
# kill_servers runs before every destructive step.
set -u

# Repo root, derived from this script's own location -- never hardcode a path here.
cd "$(dirname "$0")/.." || exit 1

SEED="$1"
WORLD="run/tfverify"

kill_servers() {
	# Only dedicated-server JVMs. The discriminator is the trailing `nogui` argument: a dev-launch
	# server has it, the game CLIENT does not -- and both otherwise share the identical loom-cache
	# command line, so matching on "loom-cache" alone would kill the user's open game.
	# ⚠️ Double quotes, not single. Bash does not nest single quotes: a '' inside a '...' string
	# CLOSES and reopens it, so "Name=''java.exe''" reached PowerShell as an unquoted
	# "Name=java.exe", which is invalid WQL -- Get-CimInstance errored and nothing was ever
	# killed. $ is escaped for bash; \s*\$ is the regex anchor PowerShell sees.
	powershell -NoProfile -Command "Get-CimInstance Win32_Process | Where-Object { \$_.Name -eq 'java.exe' -and (\$_.CommandLine -match 'runServer' -or (\$_.CommandLine -match 'loom-cache' -and \$_.CommandLine -match 'nogui\s*\$')) } | ForEach-Object { Stop-Process -Id \$_.ProcessId -Force -ErrorAction SilentlyContinue }" >/dev/null 2>&1
	sleep 6
}

# ⚠️ Truncate the log BEFORE starting a server, never after. wait_for_done greps for "Done (" in
# run/census-out.txt, and that file survives between runs -- so a leftover log from the previous
# server satisfies the grep instantly and every stage races ahead of a server that has not even
# started. The symptom is a whole seed sweep finishing in seconds with empty censuses.
start_server() {
	: > run/census-out.txt
	./gradlew runServer > run/census-out.txt 2>&1 &
}

# ⚠️ Windows releases a dead JVM's file handles ASYNCHRONOUSLY. For a few seconds after the process
# is gone the region files still refuse to delete with "Device or resource busy", so a single rm that
# aborts on first failure gives up on a world that would have been deletable a moment later. Retry.
force_rm() {
	local target="$1" i
	for i in 1 2 3 4 5 6 7 8 9 10; do
		rm -rf "$target" 2>/dev/null && return 0
		sleep 5
	done
	rm -rf "$target"   # once more, unsilenced, so the caller sees why
}

# ⚠️ The trailing `:` is load-bearing. A bash loop returns the status of the LAST COMMAND IN ITS
# BODY, not of its condition -- so ending the body with `[ $n -gt 90 ]` made this function return 1
# on every normal iteration, and `wait_for_done || exit 1` then killed the script at the exact moment
# the server finished starting. The symptom is a seed that generates a whole world and still writes
# an empty census, with no error anywhere.
wait_for_done() {
	local n=0
	until grep -qE "Done \([0-9]+ns\)|Exception in server tick|BUILD FAILED" run/census-out.txt 2>/dev/null; do
		sleep 5
		n=$((n + 1))
		[ $n -gt 90 ] && { echo "TIMEOUT waiting for server"; return 1; }
		:
	done
}

wait_for_stable_regions() {
	local prev="" cur stable=0 n=0
	while [ $stable -lt 3 ]; do
		cur=$(ls -la "$WORLD/dimensions/0/region/" 2>/dev/null | md5sum)
		if [ "$cur" = "$prev" ]; then stable=$((stable + 1)); else stable=0; fi
		prev="$cur"
		sleep 10
		n=$((n + 1))
		[ $n -gt 40 ] && break
	done
}

patch_world_type() {
	python -c "
import gzip, struct, sys
p = '$WORLD/dimensions/0/dimension.dat'
d = bytearray(gzip.open(p, 'rb').read())
old, new = b'minecraft:overworld.extended', b'twilightforest:twilightforest.default'
i = d.find(old)
if i < 0:
    print('already patched' if d.find(new) >= 0 else 'NO MATCH'); sys.exit(0)
assert struct.unpack('>H', bytes(d[i-2:i]))[0] == len(old), 'length prefix mismatch'
d[i-2:i+len(old)] = struct.pack('>H', len(new)) + new
gzip.open(p, 'wb').write(bytes(d))
print('patched')
"
}

echo "########## seed $SEED ##########"
kill_servers
force_rm "$WORLD" || { echo "world locked, aborting"; exit 1; }

python -c "
import io
p = 'run/server.properties'
s = io.open(p, encoding='utf-8').read().splitlines(True)
s = ['level-seed=$SEED\n' if l.startswith('level-seed=') else l for l in s]
io.open(p, 'w', encoding='utf-8', newline='').writelines(s)
"

# Pass 1 -- create the world so dimension.dat exists.
start_server
wait_for_done || exit 1
kill_servers

patch_world_type
force_rm "$WORLD/dimensions/0/region" || { echo "region locked, aborting"; exit 1; }

# Pass 2 -- generate for real, with the Twilight world type in place.
start_server
wait_for_done || exit 1
wait_for_stable_regions
kill_servers

echo "----- seed $SEED: generator exceptions -----"
grep -E "Exception|Caused by" run/census-out.txt | grep -v "JNDI\|Unable to delete" | head -5
echo "(none above = clean)"

echo "----- seed $SEED: structures -----"
python tools/structure_check.py "$WORLD" --dim 0
echo "----- seed $SEED: biomes + markers -----"
python tools/scan_world.py "$WORLD" --dim 0 --sea 32 --blocks 410,640,70,80,120,433,2311,2313
echo "----- seed $SEED: spawners -----"
python tools/scan_spawners.py "$WORLD" --dim 0
echo "----- seed $SEED: trees -----"
python tools/tree_check.py "$WORLD" --dim 0
