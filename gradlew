#!/usr/bin/env sh

if [ -n "$MSYSTEM" ] ; then
    case "$MSYSTEM" in
        CYGWIN* | MINGW* )
            APP_HOME=`cygpath --path --mixed "$APP_HOME"`
            CLASSPATH=`cygpath --path --mixed "$CLASSPATH"`
            ;;
    esac
fi

if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || {
        echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2
        exit 1
    }
fi

exec "$JAVACMD" org.gradle.wrapper.GradleWrapperMain "$
