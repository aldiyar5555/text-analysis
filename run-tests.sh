#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
./build.sh
java -cp out textanalysis.tests.TestRunner
