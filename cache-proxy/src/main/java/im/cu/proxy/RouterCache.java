package im.cu.proxy;

import im.cu.api.grpc.factory.CacheRouterRpcServiceFactory;
import im.cu.api.register.ZKConstants;
import im.cu.api.register.ZkClient;
import im.cu.api.utils.DumpService;
import im.cu.grpc.api.server.cache.Common;
import im.cu.grpc.api.server.cache.router.CacheRouter;
import im.cu.grpc.api.server.cache.router.CacheRouterServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by huangliming on 2021/3/18
 */
@Component
@Slf4j
public class RouterCache {

    @Autowired
    private CacheRouterRpcServiceFactory routerRpcServiceFactory;

    @Autowired
    private ZkClient zkClient;

    private volatile boolean doubleWriteFlag = false;

    private volatile Map<Integer, Common.HostAndPort> routerCache;

    private volatile Map<Integer, Common.HostAndPort> tmpRouterCache;

    @PostConstruct
    private void init() {
        this.syncRouterTable();
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            try {
                this.syncRouterTable();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    private void syncRouterTable() {
        List<String> serviceInfos = zkClient.getChildren(ZKConstants.CACHE_ROOT_PATH + "/" + ZKConstants.CACHE_ROUTER);
        String serviceInfo = serviceInfos.get(0);
        String[] split = serviceInfo.split(":");
        Common.HostAndPort hostAndPort = Common.HostAndPort.newBuilder()
                .setHost(split[0]).setPort(Integer.valueOf(split[1])).build();
        CacheRouterServiceGrpc.CacheRouterServiceBlockingStub routerStub = routerRpcServiceFactory.createBlockingStub(hostAndPort);
        CacheRouter.CacheRouterGetRes routerTable = routerStub.getRouterTable(CacheRouter.CacheRouterGetReq.newBuilder().build());
        this.routerCache = routerTable.getRouterTableMap();
    }

    public Common.HostAndPort getBySlot(Integer slot) {
        return routerCache.get(slot);
    }

    public Common.HostAndPort getTmpBySlot(Integer slot) {
        if (!doubleWriteFlag || CollectionUtils.isEmpty(tmpRouterCache)) {
            return null;
        }
        return tmpRouterCache.get(slot);
    }

    public void update(Map<Integer, Common.HostAndPort> routerCache) {
        this.routerCache = routerCache;
    }

    /**
     * 开启原始路由表，临时路由表双写
     * @param tmpRouterCache
     */
    public void openDoubleWrite(Map<Integer, Common.HostAndPort> tmpRouterCache) {
        this.doubleWriteFlag = true;
        this.tmpRouterCache = tmpRouterCache;
    }

    public void commitTmpRouterTable() {
        this.doubleWriteFlag = false;
        this.routerCache = tmpRouterCache;
    }

    public boolean getDoubleWriteFlag() {
        return this.doubleWriteFlag;
    }
}
