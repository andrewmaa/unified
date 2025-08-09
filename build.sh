#!/bin/bash
set -euo pipefail

echo "=== Unified Messaging System Build Script ==="

if ! command -v mvn >/dev/null 2>&1; then
  echo "Error: Maven is not installed. On macOS: brew install maven" >&2
  exit 1
fi

if ! command -v java >/dev/null 2>&1; then
  echo "Error: Java is not installed or not in PATH" >&2
  exit 1
fi

echo "Maven version:"; mvn -v | sed 's/^/  /'
echo "Java version:";  java -version 2>&1 | sed 's/^/  /'

echo "\nBuilding fat JAR with Maven..."
mvn -q -DskipTests clean package

echo "\n=== Build completed successfully! ==="
echo "Artifact: target/unified-messaging-1.0.0-jar-with-dependencies.jar"
echo "Run GUI: java -cp target/unified-messaging-1.0.0-jar-with-dependencies.jar com.unified.client.UnifiedGUI"
echo "Run CLI: java -jar target/unified-messaging-1.0.0-jar-with-dependencies.jar"