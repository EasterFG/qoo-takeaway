# qoo-takeaway

这个项目的一个针对餐厅的外送送餐项目，旨在为商家提供数字化送餐管理服务，为用户提供在线点单，支付等便捷服务。当前项目仅为后端实现。

## 功能说明

改项目基于Spring Boot开发，并提供如下功能

 * 菜品数据管理

 * 员工数据管理

 * 菜品分类管理

 * 每日营业数据统计

 * 对接支付宝支付

## 技术架构

* 后端：Spring Boot、MyBatis-Plus、Redis、Jwt
* 数据库：mysql
* 服务器：tomcat

## 安装说明
如果需要使用本项目，你需要进行如下操作

1. 克隆本仓库到本地：``git clone https://github.com/EasterFG/qoo-takeaway.git``

2. 安装 JDK (Java Development Kit) 和 Maven。您可以从官方网站下载并安装最新版本的 JDK 和 Maven。

3. 访问 ``/src/main/java/resources/application.yml`` 并按照提示修改配置

4. 在命令行中导航到项目根目录。

5. 运行 `mvn clean package` 命令来编译和打包程序。

## 使用说明

在编译和打包成功后，您可以在 `target` 目录下找到生成的可执行文件（如 `.jar` 文件）。您可以通过如下操作执行

1. 运行 ``cd ./tatget`` 命令进入target文件夹

2. 运行 ``java -jar qoo-takeaway-1.0.0-SNAPSHOT.jar`` 命令来运行可执行文件

3. 访问 ``localhost:8000/doc.html``

## 意见反馈
如果您在使用本项目时遇到任何问题，可以通过以下方式与我联系：
* 发送电子邮件至easterfg@163.com
* 在GitHub上面提交问题报告

## 版权信息

该项目遵循GPL-3.0 License开源协议，详情请参考 [LICENSE](https://github.com/EasterFG/qoo-takeaway/blob/main/LICENSE)。
