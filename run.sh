#!/usr/bin/env bash
set -euo pipefail

echo "=== Unified Messaging System (CLI) ==="

JAR=""
if [ -f target/app.jar ]; then
  JAR=target/app.jar
elif ls target/*-jar-with-dependencies.jar >/dev/null 2>&1; then
  JAR=$(ls target/*-jar-with-dependencies.jar | head -n1)
fi

if [ -z "${JAR}" ]; then
  echo "JAR not found. Building..."
  mvn -q -DskipTests clean package || true
  if [ -f target/app.jar ]; then
    JAR=target/app.jar
  else
    mvn -q -DskipTests assembly:single || true
    JAR=$(ls target/*-jar-with-dependencies.jar | head -n1)
  fi
fi

echo "Starting CLI (com.unified.App) from $JAR ..."
exec java -jar "$JAR"
