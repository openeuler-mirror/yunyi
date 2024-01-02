#!/bin/sh
# ./ry.sh start 启动 stop 停止 restart 重启 status 状态
AppName=console-admin.jar

# JVM参数
JVM_OPTS="-Dname=$AppName  -Duser.timezone=Asia/Shanghai -Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m"


# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# Set APP_HOME as current working directory
APP_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`
LOG_PATH=$APP_HOME/logs/$AppName.log
cd $APP_HOME

echo "APP_HOME: $APP_HOME"


if [ "$1" = "" ];
then
    echo -e "\033[0;31m 未输入操作名 \033[0m  \033[0;34m {start|run|stop|restart|status} \033[0m"
    exit 1
fi

if [ "$AppName" = "" ];
then
    echo -e "\033[0;31m 未输入应用名 \033[0m"
    exit 1
fi

function start()
{
    PID=`ps -ef |grep java|grep $AppName|grep -v grep|awk '{print $2}'`

	if [ x"$PID" != x"" ]; then
	    echo "$AppName is running..."
	else
		nohup java $JVM_OPTS -jar lib/$AppName > /dev/null 2>&1 &
		echo "Start $AppName success..."
	fi
}

function run()
{
    PID=`ps -ef |grep java|grep $AppName|grep -v grep|awk '{print $2}'`

    if [ x"$PID" != x"" ]; then
        echo "$AppName is running..."
    else
      if [ ! -d "data/db" ]; then
          echo "data/ do not contained data files! try to initial it!"
          if [ ! -d "data" ]; then
              mkdir data
          fi
          cp -a data-init/* data/
      fi

      java $JVM_OPTS -jar lib/$AppName
    fi
}

function stop()
{
    echo "Stop $AppName"

	PID=""
	query(){
		PID=`ps -ef |grep java|grep $AppName|grep -v grep|awk '{print $2}'`
	}

	query
	if [ x"$PID" != x"" ]; then
		kill -TERM $PID
		echo "$AppName (pid:$PID) exiting..."
		while [ x"$PID" != x"" ]
		do
			sleep 1
			query
		done
		echo "$AppName exited."
	else
		echo "$AppName already stopped."
	fi
}

function restart()
{
    stop
    sleep 2
    start
}

function status()
{
    PID=`ps -ef |grep java|grep $AppName|grep -v grep|wc -l`
    if [ $PID != 0 ];then
        echo "$AppName is running..."
    else
        echo "$AppName is not running..."
    fi
}

case $1 in
    start)
    start;;
    run)
    run;;
    stop)
    stop;;
    restart)
    restart;;
    status)
    status;;
    *)

esac
