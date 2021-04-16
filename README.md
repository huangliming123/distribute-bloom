# distribute-bloom
分布式布隆过滤器
类似于codis的集群方案

# Router
1. 维护路由表
2. 提供一些管理平台的功能
3. slot在各个store节点之间的迁移发起者
4. todo 目前不支持集群，只能单点

# Proxy
1. 对store集群进行代理， 根据从router获取到的路由表进行路由。 无状态，可集群

# Store
1. 存储数据的地方，目前支持两种数据类型
  1.1 全量数据
  1.2 限定在某一个时间范围内的数据，比如31天，7天


![image](https://user-images.githubusercontent.com/48201783/114971229-7fc44780-9eae-11eb-8952-feb82b199932.png)



# 技术选型
各个模块基于springboot开发
模块通讯选择了更加轻量级的grpc
