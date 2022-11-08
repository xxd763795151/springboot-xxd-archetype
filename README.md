# spring boot archetype
## 默认技术栈
* spring boot
* mybatis(xml配置)
* druid 数据源
* spring boot默认多环境配置
* logback
## 其它依赖
* hutool
* lombok

## 安装到本地仓库
```shell
mvn clean install
```
## 生成脚手架
安装本地仓库后，执行如下命令

```shell
mvn archetype:generate \
-DgroupId=com.test \
-DartifactId=demo \
-DarchetypeGroupId=com.xxd \
-DarchetypeArtifactId=springboot-archetype \
-DarchetypeVersion:1.0 \
-Dpackage=com.test.demo
```
上面的groupId等属性如果经常用模板的应该知道，就是生成的新项目的groupId，其它几个比较熟悉的字段类似，
package是生成的代码的基本包名，dir是生成的代码的目录，与包名保持一致。

创建完成后，删除src/main/java目录下和package名字一样的目录。

windows下整行命令：
```shell
mvn archetype:generate -DgroupId=com.test -DartifactId=demo -DarchetypeGroupId=com.xxd -DarchetypeArtifactId=springboot-archetype -DarchetypeVersion:1.0 -Dpackage=com.test.demo
```