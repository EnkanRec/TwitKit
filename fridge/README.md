# TwitKit Fridge 

TwitKit的数据库后端，因为是存**生肉**的，所以是冰箱。

## 依赖

### 运行依赖

- mysql或mariadb
- jre8

### 编译依赖

- jdk8
- maven

在Ubuntu 18.04下：

```shell
sudo apt install openjdk-8-jre-headless openjdk-8-jdk-headless maven -y
```

- 在`maven`的依赖关系中，虚包由这些包填实: `default-jre-headless`, `openjdk-11-jre-headless`, `openjdk-8-jre-headless`；
  也就是必须指明`openjdk-8-jre-headless`，否则就会安装`openjdk-11-jre-headless`，另一方面，要执行**编译**的话又必须依赖jdk，因此需要同时安装这三个包。
  另外，目前来看jdk11除了编译警告较多以外，不影响使用。

## 快速安装

如果当前用户具有sudo权限，并且数据库加载了`unix_socket`插件（Ubuntu 18.04的默认配置），则可以使用根目录下的脚本进行快速安装。安装完成后建议取消sudo权限。

- `test_init.sh`用于快速建立测试库，增加`--remove`参数用于移除。
- `build.sh`用于运行编译和运行测试。

- `configen.sh`用于生成模板配置文件。

- `db_init.sh`用于快速部署生产使用的数据库，执行`db_init.sh [name]`会产生同名的数据库、用户和一串随机密码（优先使用pwgen，未找到则会使用uuidgen），并将其按照配置文件的格式打印出来；没有提供参数则会使用当前登录的用户名（显然，如果root用户不带参数运行此脚本会导致报错）

- `start.sh`用于运行程序。

## 手动安装

1. 为了确保数据库后端的可用性，编译时默认会进行测试，请建立数据库、用户名和密码都为`fridge_test`的测试环境，并在库中运行`schema/test_init.sql`初始化。

2. 进入`src/fridge_src`目录后执行下列命令来编译，生成到`fridge-src/target`中。如要跳过测试，取消注释`maven.test.skip`参数。

   ```
   mvn clean
   mvn package #-Dmaven.test.skip=true
   ```

3. 为生产使用建立数据库和用户，然后用`schema/db_init.sql`初始化；

4. 在**工作目录**下建立`application.properties`文件（可使用`configen.sh`），并按以下格式写入数据库参数，替换<>的内容：

   ```
   spring.datasource.yui.jdbc-url=jdbc:mysql://localhost:3306/<DBname>?characterEncoding=u
   tf-8&useSSL=true&serverTimezone=Asia/Shanghai
   spring.datasource.yui.username=<DBuser>
   spring.datasource.yui.password=<DBpass>
   ```

5. 通过`java -jar`执行编译出的jar包。


