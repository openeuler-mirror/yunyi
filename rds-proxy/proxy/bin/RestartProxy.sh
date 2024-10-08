#! /bin/sh

RUN_DIR=$(dirname $(readlink -f $0))

echo "Stopping Proxy at `date`"

$RUN_DIR/StopProxy.sh >> /dev/null 2>&1

sleep 5

echo "Server stopped at `date`"

sleep 1

echo "Starting Proxy at `date`"

nohup $RUN_DIR/StartProxy.sh >> /dev/null 2>&1 &

sleep 2

exit 0
