#! /bin/bash
# 设置启动的环境，dev(开发), test(测试)
ENV='dev'

PARENT=$(dirname "`pwd`/$0")

PIDFILE="$PARENT/${artifactId}.pid"
#是否已启动
pid=`test -e $PIDFILE && cat $PIDFILE`
if [ -n "$pid" ]; then
  echo "Server has started. pid: $pid";
  echo "If you think this is a wrong judgement. You can execute shutdown.sh or delete $PIDFILE, then restart it.";
  exit 0;
fi

JAVA_CMD=$(which java)
if [ ! -x "$JAVA_CMD" ]; then
        echo 'no jre, then exit.';
        exit 0;
fi

TARGET="$PARENT/${artifactId}.jar"
# 设置jvm堆大小及栈大小
JAVA_MEM_OPTS="-Xmx2g -Xms2g -Xmn1g -Xss256k"

#-Dcom.sun.management.jmxremote.authenticate=false #不开启鉴权
#-Dcom.sun.management.jmxremote.ssl=false #不开启ssl
#-Dcom.sun.management.jmxremote.local.only=true #只接受本地连接
#-Dcom.sun.management.jmxremote.host=${bind_ip} #jmx绑定指定地址，默认绑定到所有地址
#-Dcom.sun.management.jmxremote.port=19006 #jmx绑定端口
#-Dcom.sun.management.jmxremote.rmi.port=19006 #rmi绑定端口，可以与jmx端口一致
#-Djava.rmi.server.hostname=${bind_ip} #rmi地址无需绑定（经测绑定后会取不到进程指标）
#-XX:+DisableAttachMechanism #禁用attach功能，无需配置（此端口不能指定，但是只有本机才能连，应该是安全的）

#设置启动参数,如 "$JAVA_OPTS -Dserver.port=18080"
JAVA_OPTS="$JAVA_OPTS $JAVA_MEM_OPTS"
JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=$ENV"

if [ ! -z $1 ]; then
  TARGET=$1;
fi

STOPSIG="${artifactId}_`date +%Y%m%d%H%M%S`"

nohup $JAVA_CMD -jar $JAVA_OPTS $TARGET $STOPSIG &>/dev/null &
sleep 1
PID=$(pgrep -f $STOPSIG)
echo "Server started: $PID, sig: $STOPSIG"
echo $PID > $PIDFILE
