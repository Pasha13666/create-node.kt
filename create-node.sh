#!/bin/sh

url="https://raw.githubusercontent.com/Pasha13666/create-node.kt/master/dist/create-node.kt.jar"
remove_jar=false

if [ -n "$JAVA_HOME" ];then
    java="$JAVA_HOME/bin/java"
elif [ -n "$JDK_HOME" ];then
    java="$JDK_HOME/bin/java"
elif java -version >/dev/null 2>&1;then
    java=java
else
    echo "Cant find java! Check your jvm installation and set \$JAVA_HOME."
    exit 1
fi

if [ -d "/tmp" -a -w "/tmp" ];then
    tmpjar="/tmp/create-node.jar"
elif [ -n "$HOME" -a -d "$HOME" -a -w "$HOME" ];then
    tmpjar="$HOME/.cache/create-node.jar"
elif [ -w "." ];then
    tmpjar="./create-node"
    remove_jar=true
else
    echo "Cannot find writable temporary directory! Try \`chmod +w .\`."
    exit 1
fi

if [ ! -f "$tmpjar" ];then
    if wget --version >/dev/null 2>&1;then
        load="wget -O "
    elif curl --version >/dev/null 2>&1;then
        load="curl -o "
    else
        echo "Cant find wget or curl! Please, install it."
        exit 1
    fi

    ${load} "$tmpjar" "$url"
    if [ $? -ne 0 ];then
        echo "Error downloading file $url! Try to download it and place to $tmpjar."
        exit 2
    fi
fi

if ${remove_jar};then
    ${java} -jar "$tmpjar" "$@"
    rm -f "$tmpjar"
else
    exec ${java} -jar "$tmpjar" "$@"
fi
