#!/bin/bash

# Unified Messaging System Build Script (Maven)

echo "=== Unified Messaging System Build Script ==="

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    echo "Please install Maven: brew install maven"
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    exit 1
fi

echo "Maven version:"
mvn --version

echo ""
echo "Java version:"
java -version

# Clean and compile with Maven
echo ""
echo "Building project with Maven..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo ""
    echo "Compilation successful!"
    
    # Run tests
    echo "Running tests..."
    mvn test
    
    # Create executable JAR
    echo ""
    echo "Creating executable JAR..."
    mvn package
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "=== Build completed successfully! ==="
        echo "Executable JAR created: target/unified-messaging-1.0.0-jar-with-dependencies.jar"
        echo ""
        echo "To run the application:"
        echo "  java -jar target/unified-messaging-1.0.0-jar-with-dependencies.jar"
        echo ""
        echo "To run tests:"
        echo "  mvn test"
        echo ""
        echo "To clean and rebuild:"
        echo "  mvn clean package"
    else
        echo "Package creation failed!"
        exit 1
    fi
else
    echo "Compilation failed!"
    exit 1
fi 