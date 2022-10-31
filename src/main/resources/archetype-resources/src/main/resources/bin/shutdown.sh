#! /bin/bash

PARENT_DIR=$(dirname "`pwd`/$0")
PIDFILE="$PARENT_DIR/pid"
pid=`test -e $PIDFILE && cat $PIDFILE`
if [  -z "$pid" ]; then
    echo "没有正在运行的进程: ${pid}";
    exit 0;
else
    kill -9 $pid;
    rm $PIDFILE;
    echo "停止服务: $pid ";
fi
