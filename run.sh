#!/bin/bash

# Unified Messaging System Run Script

echo "=== Unified Messaging System ==="

# Check if build directory exists
if [ ! -d "build" ]; then
    echo "Build directory not found. Running build script first..."
    ./build.sh
fi

# Run the application
echo "Starting Unified messaging application..."
java -cp build com.unified.App 