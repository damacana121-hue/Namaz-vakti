#!/usr/bin/env sh
set -eu
GRADLE_VERSION="9.3.1"
ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
CACHE_DIR="${HOME}/.cache/namaz-vakti/gradle-${GRADLE_VERSION}"
GRADLE_BIN="${CACHE_DIR}/gradle-${GRADLE_VERSION}/bin/gradle"
if [ ! -x "$GRADLE_BIN" ]; then
  mkdir -p "$CACHE_DIR"
  ARCHIVE="$CACHE_DIR/gradle.zip"
  curl -fL --retry 3 "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o "$ARCHIVE"
  unzip -q -o "$ARCHIVE" -d "$CACHE_DIR"
fi
exec "$GRADLE_BIN" -p "$ROOT_DIR" "$@"
