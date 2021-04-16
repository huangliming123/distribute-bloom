package im.cu.router.listener;

import im.cu.api.grpc.factory.CacheProxyRpcServiceFactory;
import im.cu.api.register.ZKConstants;
import im.cu.api.register.ZkClient;
import im.cu.grpc.api.server.cache.Common;
import im.cu.grpc.api.server.cache.proxy.CacheProxy;
import im.cu.grpc.api.server.cache.proxy.CacheProxyServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by huangliming on 2021/4/2
 */
@Component
@Slf4j
public class SlotChangeEventListener {

    @Autowired
    private CacheProxyRpcServiceFactory cacheProxyRpcServiceFactory;

    @Autowired
    private ZkClient zkClient;

    @Async
    @EventListener
    public void notifyAllProxy(SlotsChangeEvent slotsChangeEvent){
        List<String> children = zkClient.getChildren(ZKConstants.CACHE_ROOT_PATH + "/" + ZKConstants.CACHE_PROXY);
        for (String instance : children) {
            String[] split = instance.split(":");
            Common.HostAndPort hostAndPort = Common.HostAndPort.newBuilder()
                    .setHost(split[0]).setPort(Integer.valueOf(split[1])).build();
            CacheProxyServiceGrpc.CacheProxyServiceBlockingStub stub = cacheProxyRpcServiceFactory.createBlockingStub(hostAndPort);
            for (int i=0; i<3; i++) {
                try {
                    stub.syncRouterTable(CacheProxy.RouterTableSyncReq.newBuilder().putAllRouterTable(slotsChangeEvent.getRouterTab()).build());
                    break;
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }
        }
    }
}









