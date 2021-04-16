package im.cu.api.grpc.factory;

import im.cu.grpc.api.server.cache.Common;
import im.cu.grpc.api.server.cache.store.CacheStoreServiceGrpc;

/**
 * Created by huangliming on 2021/4/1
 */
public class CacheStoreRpcServiceFactory {

    private AbstractManagedChannelFactory channelFactory;

    public CacheStoreRpcServiceFactory(AbstractManagedChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
    }

    public CacheStoreServiceGrpc.CacheStoreServiceBlockingStub createBlockingStub(Common.HostAndPort hostAndPort) {
        return CacheStoreServiceGrpc.newBlockingStub(channelFactory.create(hostAndPort));
    }

    public CacheStoreServiceGrpc.CacheStoreServiceStub createAsyncStub(Common.HostAndPort hostAndPort) {
        return CacheStoreServiceGrpc.newStub(channelFactory.create(hostAndPort));
    }

    public CacheStoreServiceGrpc.CacheStoreServiceBlockingStub createBlockingStub(Common.CacheKey cacheKey) {
        return CacheStoreServiceGrpc.newBlockingStub(channelFactory.create(getSlot(cacheKey)));
    }

    public CacheStoreServiceGrpc.CacheStoreServiceStub createAsyncStub(Common.CacheKey cacheKey) {
        return CacheStoreServiceGrpc.newStub(channelFactory.create(getSlot(cacheKey)));
    }

    public CacheStoreServiceGrpc.CacheStoreServiceStub createAsyncStub(Common.CacheKey cacheKey, boolean tmp) {
        return CacheStoreServiceGrpc.newStub(channelFactory.create(getSlot(cacheKey), tmp));
    }

    public CacheStoreServiceGrpc.CacheStoreServiceBlockingStub createBlockingStub(Common.CacheKey cacheKey, boolean tmp) {
        return CacheStoreServiceGrpc.newBlockingStub(channelFactory.create(getSlot(cacheKey), tmp));
    }

    private int getSlot(Common.CacheKey cacheKey) {
        return cacheKey.getUserId() % 1024;
    }
}
