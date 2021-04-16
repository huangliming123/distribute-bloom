package im.cu.api.grpc.factory;

import im.cu.grpc.api.server.cache.Common;
import im.cu.grpc.api.server.cache.router.CacheRouterServiceGrpc;

/**
 * Created by huangliming on 2021/4/1
 */
public class CacheRouterRpcServiceFactory {

    private AbstractManagedChannelFactory channelFactory;

    public CacheRouterRpcServiceFactory(AbstractManagedChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
    }

    public CacheRouterServiceGrpc.CacheRouterServiceBlockingStub createBlockingStub(Common.HostAndPort hostAndPort) {
        return CacheRouterServiceGrpc.newBlockingStub(channelFactory.create(hostAndPort));
    }

    public CacheRouterServiceGrpc.CacheRouterServiceStub createAsyncStub(Common.HostAndPort hostAndPort) {
        return CacheRouterServiceGrpc.newStub(channelFactory.create(hostAndPort));
    }

    private int getSlot(Common.CacheKey cacheKey) {
        return cacheKey.getUserId() % 1024;
    }
}
