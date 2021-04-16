package im.cu.store.task;

import im.cu.api.grpc.factory.CacheRouterRpcServiceFactory;
import im.cu.api.register.ZKConstants;
import im.cu.api.register.ZkClient;
import im.cu.grpc.api.server.cache.Common;
import im.cu.grpc.api.server.cache.router.CacheRouter;
import im.cu.grpc.api.server.cache.router.CacheRouterServiceGrpc;
import im.cu.store.cache.FullRelationCacheImpl;
import im.cu.store.cache.LimitRelationCacheImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by huangliming on 2021/4/15
 */
@Component
public class CleanCacheTask {

    @Autowired
    private ZkClient zkClient;

    @Autowired
    private CacheRouterRpcServiceFactory routerRpcServiceFactory;

    @Autowired
    private FullRelationCacheImpl fullRelationCache;

    @Autowired
    private LimitRelationCacheImpl limitRelationCache;

    @Value("${grpc.server.port}")
    private int port;

    @Scheduled(cron = "0 0 2 * * ?")
    private void fullCache() throws UnknownHostException {
        Set<Integer> localSlots = getLocalSlots();
        fullRelationCache.cleanSlot(localSlots);
    }

    @Scheduled(cron = "0 0 3 * * ?")
    private void limitCache() throws UnknownHostException {
        Set<Integer> localSlots = getLocalSlots();
        limitRelationCache.cleanSlot(localSlots);
    }

    @Scheduled(cron = "0 0 4 * * ?")
    private void limitQueue31() {
        limitRelationCache.cleanLimitQueue();
    }

    private Set<Integer> getLocalSlots() throws UnknownHostException {
        List<String> serviceInfos = zkClient.getChildren(ZKConstants.CACHE_ROOT_PATH + "/" + ZKConstants.CACHE_ROUTER);
        String serviceInfo = serviceInfos.get(0);
        String[] split = serviceInfo.split(":");
        Common.HostAndPort hostAndPort = Common.HostAndPort.newBuilder()
                .setHost(split[0]).setPort(Integer.valueOf(split[1])).build();
        CacheRouterServiceGrpc.CacheRouterServiceBlockingStub routerStub = routerRpcServiceFactory.createBlockingStub(hostAndPort);
        CacheRouter.CacheRouterGetRes routerTable = routerStub.getRouterTable(CacheRouter.CacheRouterGetReq.newBuilder().build());
        Map<Integer, Common.HostAndPort> routerTableMap = routerTable.getRouterTableMap();
        Common.HostAndPort local = Common.HostAndPort.newBuilder().setHost(InetAddress.getLocalHost().getHostAddress()).setPort(port).build();
        Set<Integer> slots = routerTableMap.entrySet().stream()
                .filter(item -> item.getValue().equals(local)).map(item -> item.getKey()).collect(Collectors.toSet());
        return slots;
    }
}
