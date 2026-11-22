#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
# Build from source, wait for MySQL, and return the Java application's exit code.
exec docker compose up --build --abort-on-container-exit --exit-code-from app
