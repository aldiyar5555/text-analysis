#!/usr/bin/env bash
# Runs all experiments, writes results/*.csv + results/results.md, then draws plots/*.png
set -e
cd "$(dirname "$0")"
./build.sh
java -Xss16m -cp out textanalysis.bench.Benchmark results
python3 scripts/plot_results.py
