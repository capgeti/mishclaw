#!/bin/bash

echo "use the application to scan all metadata"

java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image -jar target/mishclaw-1.0.0-SNAPSHOT.jar