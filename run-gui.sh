#!/bin/bash
set -euo pipefail

echo "=== Unified Messaging System (GUI) ==="

JAR="target/unified-messaging-1.0.0-jar-with-dependencies.jar"

if [ ! -f "$JAR" ]; then
  echo "JAR not found. Building..."
  mvn -q -e -DskipTests clean package
fi

echo "Starting GUI (com.unified.client.UnifiedGUI)..."
exec java -cp "$JAR" com.unified.client.UnifiedGUI

