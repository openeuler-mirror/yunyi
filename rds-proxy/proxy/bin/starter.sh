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

SERVER_NAME="MemDBProxy"

#################################################################

wait4pid() {
  pid=$1
  time=$2

  if [ "X$pid" = "X" ]
  then
    echo "null pid"
    return
  fi

  i="0"
  while [ "$i" -le "$time" ]
  do
    i=$((i+1))
    sleep 1
    echo waited $i seconds
    newpid=`ps -ef | awk '{print $2}' | grep $pid`
    if [ "$pid" != "$newpid" ]
    then
      echo "process $pid stopped"
      exit 0
    fi
  done
}

#################################################################


# shellcheck disable=SC2045
#for file in `ls ${SERVER_HOME}/lib`
#do
#    if [ -f "${SERVER_HOME}/lib/${file}" ]
#    then
#        CLASS_PATH=${SERVER_HOME}/lib/${file}:${CLASS_PATH}
#    fi
#done

for file in `ls ${SERVER_HOME}/lib`
do
    if echo "$file" | grep -q -E '\.jar$'
    then
        if [ -f "${SERVER_HOME}/lib/${file}" ]
        then
            CLASS_PATH=${SERVER_HOME}/lib/${file}:${CLASS_PATH}
        fi
    fi
done

#if [ -f "/bin/uname" ]
#then
#    UNIX_NAME=`/bin/uname`
#elif [ -f "/usr/bin/uname" ]
#then
#    UNIX_NAME=`/usr/bin/uname`
#fi
#
#if [ "X$UNIX_NAME" = "XAIX" ]
#then
#    SERVEROPT=" -Xmx35g -Xgcpolicy:gencon -Xverbosegclog:${SERVER_HOME}/logs/server.gc "
#    #SERVEROPT=" -Xmx35g -Xgcpolicy:optavgpause "
#else
#    #SERVEROPT=" -Xmx35g -Xmn1g -d64 -server -XX:+UseConcMarkSweepGC -XX:+UseCMSCompactAtFullCollection -XX:+UseParNewGC -XX:+CMSParallelRemarkEnabled -XX:+CMSClassUnloadingEnabled -verbose:gc -XX:+PrintGCDetails -Xloggc:${SERVER_HOME}/logs/server.gc "
#    SERVEROPT=" -Xmx35g -server -XX:+CMSParallelRemarkEnabled -XX:+CMSClassUnloadingEnabled "
#fi

if [ "$1" != "stop" ]
then
    if [ -f ${SERVER_HOME}/bin/external.vmoptions ]
    then
        SERVEROPT=`cat ${SERVER_HOME}/bin/external.vmoptions | grep -v '^[	 ]*#' | xargs`
    fi

    SERVEROPT=" -Dserver=${SERVER_NAME} -Dserver.home=${SERVER_HOME} -Djdk.tls.rejectClientInitiatedRenegotiation=true ${SERVEROPT} "

#    if [ -f ${SERVER_HOME}/monitor-agent.jar ]
#    then
#        SERVEROPT="${SERVEROPT} -javaagent:${SERVER_HOME}/monitor-agent.jar "
#    fi
else
    echo "stop server: home=${SERVER_HOME}"

    pidcmd="ps -ef | grep 'server=${SERVER_NAME} ' | grep 'server.home=${SERVER_HOME} '"

    pid=`eval $pidcmd | awk '{print $2}'`

    if [ "$pid" != "" ]
    then
        echo "The PID of the stopped process is $pid"
        kill $pid

        wait4pid "$pid" 60

        echo "stop the process force"
        kill -9 $pid
    fi
    echo "Server stopped."
fi

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

$JAVA ${SERVEROPT} -classpath "$CLASS_PATH" com.tongtech.proxy.Proxy $1
