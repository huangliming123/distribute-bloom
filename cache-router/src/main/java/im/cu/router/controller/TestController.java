package im.cu.router.controller;

import im.cu.api.grpc.factory.CacheProxyRpcServiceFactory;
import im.cu.api.monitor.Durating;
import im.cu.api.register.ZkClient;
import im.cu.grpc.api.server.cache.Common;
import im.cu.grpc.api.server.cache.proxy.CacheProxy;
import im.cu.grpc.api.server.cache.proxy.CacheProxyServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;

/**
 * Created by huangliming on 2021/3/26
 */
@RestController
@RequestMapping("test")
@Slf4j
public class TestController {

    @Autowired
    private CacheProxyRpcServiceFactory proxyRpcServiceFactory;

    @Autowired
    private ZkClient zkClient;

    @PostMapping(value = "/import-full-cache")
    public void importFullCache() {
        log.info("执行aloha数据导入测试 1000_0000数据 ...");
        List<String> children = zkClient.getChildren("/finka-smart-cache/cache-proxy");
        String[] split = children.get(0).split(":");
        CacheProxyServiceGrpc.CacheProxyServiceBlockingStub proxy
                = proxyRpcServiceFactory.createBlockingStub(Common.HostAndPort.newBuilder()
                .setHost(split[0]).setPort(Integer.valueOf(split[1])).build());
        Random random = new Random();
        for (long i=0; i<1000000; i++) {
            int userId = random.nextInt(400_0000);
            proxy.add(CacheProxy.CacheProxyAddReq.newBuilder()
                    .setCacheType(Common.CacheType.Full).setCacheKey(Common.CacheKey.newBuilder()
                            .setPrefix("aloha").setUserId(userId).build()).setValue(random.nextInt()).build());
        }
    }

    @PostMapping(value = "/import-limit-cache")
    public void importLimitCache() {
        log.info("执行aloha数据导入测试 1000_0000数据 ...");
        CacheProxyServiceGrpc.CacheProxyServiceBlockingStub proxy
                = proxyRpcServiceFactory.createBlockingStub(Common.HostAndPort.newBuilder()
                .setHost("192.168.1.3").setPort(19090).build());
        Random random = new Random();
        for (long i=0; i<400_0000; i++) {
            int userId = random.nextInt(400_0000);
            proxy.add(CacheProxy.CacheProxyAddReq.newBuilder()
                    .setCacheType(Common.CacheType.Limit).setCacheKey(Common.CacheKey.newBuilder()
                            .setPrefix("aloha").setUserId(userId).build()).setValue(random.nextInt()).build());
        }
    }
}
