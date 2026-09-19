#!/usr/bin/env bash
# Runs the order demo on minispring and on Spring Boot, then compares what they print.
# Startup times go to stderr, so they are shown but not compared.
set -euo pipefail
cd "$(dirname "$0")/.."

./mvnw -q install -DskipTests
minispring_output=$(./mvnw -q -pl demo exec:exec)
spring_output=$(./mvnw -q -pl demo-spring exec:exec)

if diff <(echo "$minispring_output") <(echo "$spring_output"); then
    echo "Both containers printed the same $(echo "$minispring_output" | wc -l | tr -d ' ') lines."
else
    echo "The outputs differ." >&2
    exit 1
fi
