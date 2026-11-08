#!/usr/bin/env bash
# Compile all sources (main + tests) into ./out
set -e
cd "$(dirname "$0")"
rm -rf out
mkdir -p out
javac -d out $(find src -name "*.java")
echo "Compiled into ./out"
