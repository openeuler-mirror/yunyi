#!/bin/sh
#
#################################################################
#
# SM4 start script
#
#################################################################

if [ "X$SERVER_HOME" = "X" ]
then
    SERVER_HOME=$(dirname $(readlink -f $0))
    if [ ! -f "${SERVER_HOME}/etc/proxy.xml" ]
    then
         SERVER_HOME=$(dirname $SERVER_HOME)
    fi
fi

if [ "X$JAVA_HOME" = "X" ]
then
    JAVA_HOME=/usr/;export JAVA_HOME
fi

#################################################################

# shellcheck disable=SC2045
for file in `ls ${SERVER_HOME}/lib`
do
    if [ -f "${SERVER_HOME}/lib/${file}" ]
    then
        CLASSPATH=${CLASSPATH}:${SERVER_HOME}/lib/${file}
    fi
done

JAVA=`which java`

if [ "X${JAVA}" = "X" ]
then
    JAVA=${JAVA_HOME}/bin/java
fi

if [ ! -f "${JAVA}" ] ; then
    echo ERROR: JAVA was not found
    exit 1
fi

$JAVA -classpath "$CLASSPATH" com.tongtech.proxy.SM3  "$1" "$2"
