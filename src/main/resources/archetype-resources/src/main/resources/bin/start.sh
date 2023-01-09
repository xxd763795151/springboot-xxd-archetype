#! /bin/bash

# 设置堆内存及相关jvm属性
JAVA_MEM_OPTS="-Xmx256M -Xms256M -Xmn128M -Xss256k"
JAVA_OPTS="$JAVA_OPTS $JAVA_MEM_OPTS"
JAVA_OPTS="$JAVA_OPTS -Dfile.encoding=utf-8"
PARENT_DIR=$(dirname "`pwd`/$0")
PIDFILE="$PARENT_DIR/pid"
LIB_PATH="$PARENT_DIR/lib"
TARGET_JAR="`find $LIB_PATH -iname *.jar`"
#CONFIG_LOCATION="$PARENT_DIR/config/application.yml"
CONFIG_LOCATION="$PARENT_DIR/config/"
JAVA_OPTS="$JAVA_OPTS -Dspring.config.location=classpath:/,classpath:config/,${CONFIG_LOCATION}"
JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=@app.env@"

# jmx配置
#不开启鉴权
#JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.authenticate=false"
#不开启ssl
#JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.ssl=false"
#只接受本地连接
#JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.local.only=true"
#默认绑定到所有地址
#JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.host=0.0.0.0"
#jmx绑定端口
#JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.port=1099"
#-Dcom.sun.management.jmxremote.rmi.port=19006 #rmi绑定端口，可以与jmx端口一致
#-Djava.rmi.server.hostname=${bind_ip} #rmi地址无需绑定（经测绑定后会取不到进程指标）
#-XX:+DisableAttachMechanism #禁用attach功能，无需配置（此端口不能指定，但是只有本机才能连，应该是安全的）

START_IN_FRONT=""
function usage() {
    echo "用法说明: sh start.sh [OPTIONS]"
    echo "-f    前台启动，e.g. sh start.sh -f 前台启动服务，不加参数，默认后台运行"
}
while [ ! $# -eq 0 ]
do
  case $1 in
  -h|--help)
    shift
    usage
    exit 1
    ;;
  -f)
    START_IN_FRONT="true"
    shift
    ;;
  *)
    echo "忽略参数$1"
    shift
    ;;
  esac
done

pid=`test -e $PIDFILE && cat $PIDFILE`
if [ -n "$pid" ]; then
  echo "服务已启动. 进程ID: $pid";
  echo "如果这是一个错误的判断可以执行shutdown.sh关闭进程，或者删除进程文件 $PIDFILE, 然后重新启动.";
  exit 0;
fi

JAVA_CMD=$(which java)
if [ ! -x "$JAVA_CMD" ]; then
        echo 'no jre, then exit.';
        exit 0;
fi

STOPSIG="$(basename $TARGET_JAR .jar)_`date +%Y%m%d%H%M%S`"

if [ -n "$START_IN_FRONT" ];then
  $JAVA_CMD $JAVA_OPTS -jar  $TARGET_JAR
else
  nohup $JAVA_CMD $JAVA_OPTS -jar  $TARGET_JAR $STOPSIG 1>/dev/null 2>${PARENT_DIR}/start_error.out &
  sleep 2
  PID=$(pgrep -f $STOPSIG)
  echo "服务已启动: $PID, sig: $STOPSIG"
  echo $PID > $PIDFILE
fi