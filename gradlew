#!/bin/sh
APP_HOME="$(cd "$(dirname "$0")" && pwd)"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"

# Find gradle 8.4 wrapper
GRADLE_DIR="$GRADLE_USER_HOME/wrapper/dists/gradle-8.4-bin"
if [ -d "$GRADLE_DIR" ]; then
    GRADLE_BIN=$(find "$GRADLE_DIR" -name "gradle" -path "*/gradle-8.4/bin/gradle" | head -1)
    if [ -x "$GRADLE_BIN" ]; then
        exec "$GRADLE_BIN" "$@"
    fi
fi

# Fallback: try to use gradle from PATH
if command -v gradle &> /dev/null; then
    exec gradle "$@"
fi

# Final fallback: use Java to run wrapper
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
JAVA_OPTS="-Xmx2048m"
exec java $JAVA_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
