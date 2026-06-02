#!/bin/sh
# Minimal Gradle wrapper launcher. Requires gradle/wrapper/gradle-wrapper.jar to
# exist locally — generate it once with:
#   gradle wrapper --gradle-version 8.7
set -e
DIR="$(cd "$(dirname "$0")" && pwd)"
JAR="$DIR/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$JAR" ]; then
  echo "gradle-wrapper.jar missing. Run: gradle wrapper --gradle-version 8.7" >&2
  exit 1
fi
exec "${JAVA_HOME:+$JAVA_HOME/bin/}java" -classpath "$JAR" org.gradle.wrapper.GradleWrapperMain "$@"
