#! /bin/bash

PARENT=$(dirname "`pwd`/$0")

PIDFILE="$PARENT/${artifactId}.pid"
#是否已启动
pid=`test -e $PIDFILE && cat $PIDFILE`
if [  -z "$pid" ]; then
    echo "No process is running";
    exit 0;
else
    kill -9 $pid;
    rm $PIDFILE;
    echo "Stop server: $pid ";
fi
