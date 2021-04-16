package im.cu.proxy.server.grpc;

import im.cu.api.grpc.GrpcServer;
import im.cu.api.grpc.factory.CacheStoreRpcServiceFactory;
import im.cu.api.monitor.Durating;
import im.cu.grpc.api.server.cache.Common;
import im.cu.grpc.api.server.cache.proxy.CacheProxy;
import im.cu.grpc.api.server.cache.proxy.CacheProxyServiceGrpc;
import im.cu.grpc.api.server.cache.store.CacheStore;
import im.cu.grpc.api.server.cache.store.CacheStoreServiceGrpc;
import im.cu.proxy.RouterCache;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by huangliming on 2021/4/1
 */
@Component
@GrpcServer
public class GrpcCacheProxyServer extends CacheProxyServiceGrpc.CacheProxyServiceImplBase {

    @Autowired
    private CacheStoreRpcServiceFactory cacheStoreRpcServiceFactory;

    @Autowired
    private RouterCache routerCache;

    @Durating
    @Override
    public void add(CacheProxy.CacheProxyAddReq request, StreamObserver<CacheProxy.CacheProxyAddRes> responseObserver) {
        CacheStoreServiceGrpc.CacheStoreServiceBlockingStub blockingStub
                = cacheStoreRpcServiceFactory.createBlockingStub(request.getCacheKey());
        blockingStub.add(
                CacheStore.CacheStoreAddReq.newBuilder()
                        .setCacheType(request.getCacheType())
                        .setCacheKey(request.getCacheKey())
                        .setValue(request.getValue()).setDate(request.getDate()).build()
        );
        responseObserver.onNext(CacheProxy.CacheProxyAddRes.newBuilder().setResponseCode(Common.ResponseCode.OK).build());
        responseObserver.onCompleted();
    }

    @Durating
    @Override
    public void findContains(CacheProxy.CacheProxyFilterReq request, StreamObserver<CacheProxy.CacheProxyFilterRes> responseObserver) {
        CacheStoreServiceGrpc.CacheStoreServiceBlockingStub blockingStub
                = cacheStoreRpcServiceFactory.createBlockingStub(request.getCacheKey());
        CacheStore.CacheStoreFilterRes contains = blockingStub.findContains(
                CacheStore.CacheStoreFilterReq.newBuilder()
                        .setCacheKey(request.getCacheKey())
                        .addAllValues(request.getValuesList())
                        .setCacheConfig(request.getCacheConfig()).build()
        );
        responseObserver.onNext(CacheProxy.CacheProxyFilterRes.newBuilder().addAllValues(contains.getValuesList()).build());
        responseObserver.onCompleted();
    }

    @Durating
    @Override
    public void findNotContains(CacheProxy.CacheProxyFilterReq request, StreamObserver<CacheProxy.CacheProxyFilterRes> responseObserver) {
        CacheStoreServiceGrpc.CacheStoreServiceBlockingStub blockingStub
                = cacheStoreRpcServiceFactory.createBlockingStub(request.getCacheKey());
        CacheStore.CacheStoreFilterRes notContains = blockingStub.findNotContains(
                CacheStore.CacheStoreFilterReq.newBuilder()
                        .setCacheKey(request.getCacheKey())
                        .addAllValues(request.getValuesList())
                        .setCacheConfig(request.getCacheConfig()).build()
        );
        responseObserver.onNext(CacheProxy.CacheProxyFilterRes.newBuilder().addAllValues(notContains.getValuesList()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void syncRouterTable(CacheProxy.RouterTableSyncReq request, StreamObserver<CacheProxy.RouterTableSyncRes> responseObserver) {
        routerCache.update(request.getRouterTableMap());
        responseObserver.onNext(CacheProxy.RouterTableSyncRes.newBuilder().setResponseCode(Common.ResponseCode.OK).build());
        responseObserver.onCompleted();
    }

    @Override
    public void beginMigrating(CacheProxy.RouterTableSyncReq request, StreamObserver<CacheProxy.RouterTableSyncRes> responseObserver) {
        routerCache.openDoubleWrite(request.getRouterTableMap());
        responseObserver.onNext(CacheProxy.RouterTableSyncRes.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void commitMigrating(CacheProxy.RouterTableSyncReq request, StreamObserver<CacheProxy.RouterTableSyncRes> responseObserver) {
        routerCache.commitTmpRouterTable();
        responseObserver.onNext(CacheProxy.RouterTableSyncRes.newBuilder().build());
        responseObserver.onCompleted();
    }
}
