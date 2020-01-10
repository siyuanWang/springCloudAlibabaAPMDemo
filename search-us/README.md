### WARNING
开发之前必须理解US的架构思想，不允许破坏架构。**不符合规范，会被无情的删代码**。
### US WIKI
- US架构文档： https://wiki.mafengwo.cn/pages/viewpage.action?pageId=43270662
### US开发准则
- US不允许提供任何的Dubbo接口，只能提供Http Restful接口
- US是无状态服务，不允许依赖任何本地资源
- US开发必须准守规定的设计模式，具体请参看US详细设计文档

### US依赖模块
- search-dao
- search-bs
- search-as

## 启动
在服务器上启动，首先将对应的 jar 包打好并上传到对应服务器上，使用如下方式启动
```$sh
export ALIBABA_ALIWARE_ENDPOINT_PORT=80
nohup java -Dspring.profiles.active=prod -jar search-us.jar 2>&1 &
```
- 第一行指定注册中心端点配置确保 dubbo 正常运行
- java 后的指令是指定对应的环境配置，例如：prod 为正式环境
