#!/usr/bin/env bash
# Superset Run: install the debug build on a connected device/emulator
# and launch the app. adb may not be on PATH, so resolve it from
# local.properties (sdk.dir) or ANDROID_HOME.
set -euo pipefail

sdk_dir="$(sed -n 's/^sdk\.dir=//p' local.properties 2>/dev/null || true)"
sdk_dir="${sdk_dir:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}"
adb="$sdk_dir/platform-tools/adb"
[[ -x "$adb" ]] || adb=adb

devices=$("$adb" devices | awk 'NR>1 && $2=="device" {print $1}')
if [[ -z "$devices" ]]; then
  echo "ERROR: no device/emulator connected (adb devices is empty)." >&2
  echo "Start an emulator or plug in a device with USB debugging on." >&2
  exit 1
fi
echo "Device(s): $devices"

./gradlew :app:installDebug

"$adb" shell am start -n cz.vanama.courtflow/.MainActivity

# The process needs a moment to spawn before pidof can see it.
pid=""
for _ in $(seq 1 20); do
  pid="$("$adb" shell pidof -s cz.vanama.courtflow | tr -d '\r')"
  [[ -n "$pid" ]] && break
  sleep 0.5
done
if [[ -z "$pid" ]]; then
  echo "App installed and intent sent, but the process didn't appear — check the device." >&2
  exit 1
fi
echo "CourtFlow launched (pid $pid). Logcat follows (Ctrl+C to stop):"
"$adb" logcat --pid "$pid"
