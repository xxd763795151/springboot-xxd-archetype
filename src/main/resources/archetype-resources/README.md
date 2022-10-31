# ${artifactId}

# 打包
```shell
mvn clean package -Pdev -Dmaven.test.skip=true
```
打包的时候指定环境, e.g. -Pdev, -Ptest


# 部署
打包成功，target目录下有安装包，上传到服务器，解压缩，如下：
```shell
tar -zxvf ${artifactId}.tar.gz
```

## 启动
```shell
sh start.sh
```

## 停止
```shell
sh shutdown.sh
```