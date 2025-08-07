#!/bin/bash

# Unified Messaging System Build Script

echo "=== Unified Messaging System Build Script ==="

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    exit 1
fi

# Check if javac is installed
if ! command -v javac &> /dev/null; then
    echo "Error: Java compiler (javac) is not installed or not in PATH"
    exit 1
fi

echo "Java version:"
java -version

# Create build directory
echo "Creating build directory..."
mkdir -p build

# Compile all Java files
echo "Compiling Java files..."
cd src/main/java
javac -d ../../../build com/unified/util/PasswordManager.java com/unified/model/*.java com/unified/App.java com/unified/TestApp.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    
    # Run test
    echo "Running basic functionality test..."
    java -cp ../../../build com.unified.TestApp
    
    echo ""
    echo "=== Build completed successfully! ==="
    echo "To run the application:"
    echo "  java -cp build com.unified.App"
    echo ""
    echo "To run the test:"
    echo "  java -cp build com.unified.TestApp"
else
    echo "Compilation failed!"
    exit 1
fi 