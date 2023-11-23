# BF1自动管服工具
## 运行环境
- 需要 Java 11 或更高版本
## 使用方法
使用命令行启动
```bash
java -jar .\BF1AutoManageServer-1.0-SNAPSHOT.jar
```
### 在控制台``help``查看帮助
### 首次使用请添加一个服务器
  1. 使用这行命令添加服务器``add 服务器gameid 管服号sessionID``
  2. 然后使用``stop``关闭控制台
### 然后去修改配置文件
  1. 在jar所在目录下找到setting文件夹
  2. 找到命名为``ServerSetting_{服务器gameId}.yaml``的文本文件
  3. 用notepad3、notepad++、sublime等文本编辑器打开该文件并编辑
  4. 编辑完保存然后启动服务器即可
## 鸣谢
  1. [22](https://github.com/bili-22) 提供的玩家列表服务以及部分Api
## 协议
MIT