package im.cu.api.grpc.factory;

import im.cu.grpc.api.server.cache.Common;
import im.cu.grpc.api.server.cache.proxy.CacheProxyServiceGrpc;

/**
 * Created by huangliming on 2021/4/1
 */
public class CacheProxyRpcServiceFactory {

    private AbstractManagedChannelFactory channelFactory;

    public CacheProxyRpcServiceFactory(AbstractManagedChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
    }

    public CacheProxyServiceGrpc.CacheProxyServiceBlockingStub createBlockingStub(Common.HostAndPort hostAndPort) {
        return CacheProxyServiceGrpc.newBlockingStub(channelFactory.create(hostAndPort));
    }

    public CacheProxyServiceGrpc.CacheProxyServiceStub createAsyncStub(Common.HostAndPort hostAndPort) {
        return CacheProxyServiceGrpc.newStub(channelFactory.create(hostAndPort));
    }

    private int getSlot(Common.CacheKey cacheKey) {
        return cacheKey.getUserId() % 1024;
    }
}
