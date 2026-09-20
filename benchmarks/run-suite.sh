#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MODULES=(
  collection-threshold
  map-threshold
  queue-drain
  executor-queue
)

for module in "${MODULES[@]}"; do
  echo
  echo "===== $module: original tool ====="
  (cd "$ROOT" && mvn -q -f TSVD4J-original/pom.xml install >/dev/null)
  (cd "$ROOT" && mvn -q -f "benchmarks/$module" test tsvd4j:tsvd4j || true)

  echo
  echo "===== $module: first-iteration tool ====="
  (cd "$ROOT" && mvn -q -f TSVD4J-first-iteration/pom.xml install >/dev/null)
  (cd "$ROOT" && mvn -q -f "benchmarks/$module" test tsvd4j:tsvd4j || true)
  echo
  echo "----------------------------------------"
done
