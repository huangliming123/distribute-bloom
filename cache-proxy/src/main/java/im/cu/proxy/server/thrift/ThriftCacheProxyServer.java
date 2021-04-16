package im.cu.proxy.server.thrift;

import im.cu.api.grpc.factory.CacheStoreRpcServiceFactory;
import im.cu.api.match_smart_cache.thrift.*;
import im.cu.api.monitor.Durating;
import im.cu.api.utils.DelayRetry;
import im.cu.grpc.api.server.cache.Common;
import im.cu.grpc.api.server.cache.store.CacheStore;
import im.cu.grpc.api.server.cache.store.CacheStoreServiceGrpc;
import im.cu.proxy.RouterCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by huangliming on 2021/4/1
 */
@Component
@Slf4j
public class ThriftCacheProxyServer implements CacheProxyService.Iface {

    @Autowired
    private CacheStoreRpcServiceFactory cacheStoreRpcServiceFactory;

    @Autowired
    private RouterCache routerCache;

    @Override
    @Durating
    @DelayRetry(responseType = CacheProxyAddRes.class)
    public CacheProxyAddRes add(CacheProxyAddReq req) {
        this.doAdd(req, false);
        if (routerCache.getDoubleWriteFlag()) {
            this.doAdd(req, true);
        }
        return new CacheProxyAddRes();
    }

    private void doAdd(CacheProxyAddReq req, boolean tmp) {
        CacheStoreServiceGrpc.CacheStoreServiceBlockingStub blockingStub
                = cacheStoreRpcServiceFactory.createBlockingStub(
                Common.CacheKey.newBuilder().setPrefix(req.getKey().getPrefix()).setUserId(req.getKey().getUserId()).build(), tmp
        );
        blockingStub.add(
                CacheStore.CacheStoreAddReq.newBuilder()
                        .setCacheType(Common.CacheType.forNumber(req.getCacheType().getValue()))
                        .setCacheKey(Common.CacheKey.newBuilder().setPrefix(req.getKey().prefix).setUserId(req.getKey().userId).build())
                        .setValue(req.value).setDate(req.date).build()
        );
    }

    @Override
    @Durating
    public CacheProxyFilterRes findContains(CacheProxyFilterReq req) throws TException {
        CacheStoreServiceGrpc.CacheStoreServiceBlockingStub blockingStub
                = cacheStoreRpcServiceFactory.createBlockingStub(
                Common.CacheKey.newBuilder().setPrefix(req.getKey().getPrefix()).setUserId(req.getKey().getUserId()).build()
        );
        Common.CacheConfig cacheConfig = Common.CacheConfig.newBuilder()
                .setCacheType(Common.CacheType.forNumber(req.getCacheConfig().cacheType.getValue()))
                .setDays(req.getCacheConfig().days).build();
        CacheStore.CacheStoreFilterRes contains = blockingStub.findContains(CacheStore.CacheStoreFilterReq.newBuilder()
                .setCacheKey(Common.CacheKey.newBuilder().setPrefix(req.getKey().getPrefix()).setUserId(req.getKey().getUserId()).build())
                .setCacheConfig(cacheConfig)
                .addAllValues(req.getValues()).build());
        return new CacheProxyFilterRes(contains.getValuesList());
    }

    @Override
    @Durating
    public CacheProxyFilterRes findNotContains(CacheProxyFilterReq req) throws TException {
        CacheProxyFilterRes contains = this.findContains(req);
        req.getValues().removeAll(contains.values);
        return new CacheProxyFilterRes(req.getValues());
    }
}
