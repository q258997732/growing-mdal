#!/bin/bash
set -e

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f1)
if [[ "$JAVA_VERSION" =~ ^1$ ]]; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f2)
fi

if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "[ERROR] Java 17 or higher is required. Current version: $(java -version 2>&1 | awk -F '"' '/version/ {print $2}')"
    exit 1
fi

echo "[INFO] Java $JAVA_VERSION detected, starting growing-mdal..."
java -jar growing-mdal-1.0.3.jar "$@"
