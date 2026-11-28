#!/bin/sh
set -eu
# Quotes preserve filters containing spaces; Java validates the report and input values.
if [ "$#" -lt 1 ] || [ "$#" -gt 4 ]; then
    echo 'Usage: ./scripts/report.sh REPORT_ID_OR_all [N] [FILTER] [COUNTRY]' >&2
    exit 1
fi
export REPORT="$1" TOP_N="${2:-10}" FILTER="${3:-}" COUNTRY="${4:-}"
cd "$(dirname "$0")/.."
exec ./scripts/run.sh
