#!/usr/bin/env sh
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ ! -e "$CLASSPATH" ]; then
    echo "Downloading gradle-wrapper.jar..."
    mkdir -p "$APP_HOME/gradle/wrapper"
    curl -s -L -o "$CLASSPATH" "https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar"
fi
exec java -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"