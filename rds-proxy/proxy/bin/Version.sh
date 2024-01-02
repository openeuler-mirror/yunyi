#!/bin/sh
#
#################################################################
#
# Start script for MemDB
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

SERVER_NAME="MemoryDB"


# shellcheck disable=SC2045
#for file in `ls ${SERVER_HOME}/lib`
#do
#    if [ -f "${SERVER_HOME}/lib/${file}" ]
#    then
#        CLASSPATH=${SERVER_HOME}/lib/${file}:${CLASSPATH}
#    fi
#done

for file in `ls ${SERVER_HOME}/lib`
do
    if echo "$file" | grep -q -E '\.jar$'
    then
        if [ -f "${SERVER_HOME}/lib/${file}" ]
        then
            CLASSPATH=${SERVER_HOME}/lib/${file}:${CLASSPATH}
        fi
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

cd ${SERVER_HOME}

mkdir logs > /dev/null 2>&1

$JAVA ${SERVEROPT} -classpath "$CLASSPATH" com.tongtech.proxy.Proxy version
