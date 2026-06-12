#!/usr/bin/env bash
# Superset workspace setup for CourtFlow.
# local.properties is gitignored, so a fresh worktree doesn't have it —
# without it the build can't find the Android SDK and the balldontlie API
# key (every request would return 401). Copy it from the root checkout.
set -euo pipefail

if [[ -f "$SUPERSET_ROOT_PATH/local.properties" ]]; then
  cp "$SUPERSET_ROOT_PATH/local.properties" local.properties
  echo "Copied local.properties from root checkout."
else
  sdk_dir="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
  {
    echo "sdk.dir=$sdk_dir"
    echo "balldontlie.apiKey="
  } > local.properties
  echo "WARNING: $SUPERSET_ROOT_PATH/local.properties not found." >&2
  echo "Created a stub — fill in balldontlie.apiKey or the app gets 401s." >&2
fi
