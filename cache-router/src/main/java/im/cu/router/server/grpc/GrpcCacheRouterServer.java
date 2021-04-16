package im.cu.router.server.grpc;

import im.cu.api.grpc.GrpcServer;
import im.cu.grpc.api.server.cache.Common;
import im.cu.grpc.api.server.cache.router.CacheRouter;
import im.cu.grpc.api.server.cache.router.CacheRouterServiceGrpc;
import im.cu.router.SlotService;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by huangliming on 2021/4/6
 */
@Component
@GrpcServer
public class GrpcCacheRouterServer extends CacheRouterServiceGrpc.CacheRouterServiceImplBase {

    @Autowired
    private SlotService slotService;

    @Override
    public void getRouterTable(CacheRouter.CacheRouterGetReq request, StreamObserver<CacheRouter.CacheRouterGetRes> responseObserver) {
        Map<Integer, Common.HostAndPort> routerTable = slotService.getRouterTable();
        responseObserver.onNext(CacheRouter.CacheRouterGetRes.newBuilder().putAllRouterTable(routerTable).build());
        responseObserver.onCompleted();
    }
}
