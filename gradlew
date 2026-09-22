#!/bin/sh
# Gradle start up script for UN*X
APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
MAX_FD="maximum"
warn () { echo "$*"; }
die () { echo; echo "$*"; echo; exit 1; }
# OS specific support
cygwin=false
msys=false
darwin=false
nonstop=false
case "$(uname)" in
  CYGWIN* ) cygwin=true ;;
  Darwin* ) darwin=true ;;
  MINGW* ) msys=true ;;
  NONSTOP* ) nonstop=true ;;
esac
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
# Determine the Java command to use
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
fi
# Escape application args
save () { for i do printf %s\\n "$i" | sed "s/'/'\\\\''/g;1s/^/'/;\$s/\$/' \\\\/" ; done; echo " "; }
APP_ARGS=$(save "$@")
eval set -- $APP_ARGS
# Collect all arguments for the java command
DEFAULT_JVM_OPTS_PARTS=
eval "set -- $DEFAULT_JVM_OPTS"
for arg in "$@"; do DEFAULT_JVM_OPTS_PARTS="$DEFAULT_JVM_OPTS_PARTS \"$arg\""; done
set -- $DEFAULT_JVM_OPTS_PARTS $JAVA_OPTS $GRADLE_OPTS "\"-Dorg.gradle.appname=$APP_BASE_NAME\"" -classpath "\"$CLASSPATH\"" org.gradle.wrapper.GradleWrapperMain "$APP_ARGS"
exec "$JAVACMD" "$@"
