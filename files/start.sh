#!/bin/sh

if [ -n "$JAVA_HOME" ];then
    java="$JAVA_HOME/bin/java"
else
    java=java
fi

if [ "$1" = --compile ];then
    shift
    kotlinc-jvm -cp "build`find lib '(' -name '*.jar' -o -type d -not -samefile lib ')' -printf ':%p'`" -d build `find src -name '*.kt -printf '%p '` "$@"
else
    exec "$java" -cp "build`find lib '(' -name '*.jar' -o -type d -not -samefile lib ')' -printf ':%p'`" "$@"
fi

