package im.cu.store.server.grpc;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import im.cu.api.bloom.LimitQueue;
import im.cu.api.bloom.ScaleBloomFilter;
import im.cu.api.grpc.GrpcServer;
import im.cu.api.grpc.factory.CacheStoreRpcServiceFactory;
import im.cu.api.monitor.Durating;
import im.cu.api.utils.ProtobufUtil;
import im.cu.grpc.api.server.cache.Common;
import im.cu.grpc.api.server.cache.store.CacheStore;
import im.cu.grpc.api.server.cache.store.CacheStoreServiceGrpc;
import im.cu.store.cache.FullRelationCache;
import im.cu.store.cache.LimitRelationCache;
import im.cu.store.cache.RelationCache;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by huangliming on 2021/3/16
 */

@Component
@Slf4j
@GrpcServer
public class GrpcCacheStoreServer extends CacheStoreServiceGrpc.CacheStoreServiceImplBase {

    @Autowired
    private FullRelationCache fullRelationCache;

    @Autowired
    private LimitRelationCache limitRelationCache;

    @Autowired
    private CacheStoreRpcServiceFactory cacheStoreRpcServiceFactory;

    private Map<Integer, Common.HostAndPort> slotMigratingMap = new HashMap<>();

    @Durating
    @Override
    public void add(CacheStore.CacheStoreAddReq request, StreamObserver<CacheStore.CacheStoreAddRes> responseObserver) {
        if (request.getCacheType() == Common.CacheType.Full) {
            fullRelationCache.add(request.getCacheKey(), request.getValue());
        } else {
            limitRelationCache.add(request.getCacheKey(), request.getValue(), request.getDate());
        }
        responseObserver.onNext(CacheStore.CacheStoreAddRes.newBuilder().setResponseCode(Common.ResponseCode.OK).build());
        responseObserver.onCompleted();
    }

    @Durating
    @Override
    public void findContains(CacheStore.CacheStoreFilterReq request, StreamObserver<CacheStore.CacheStoreFilterRes> responseObserver) {
        responseObserver.onNext(CacheStore.CacheStoreFilterRes.newBuilder().addAllValues(this.findContains(request)).build());
        responseObserver.onCompleted();
    }

    @Durating
    @Override
    public void findNotContains(CacheStore.CacheStoreFilterReq request, StreamObserver<CacheStore.CacheStoreFilterRes> responseObserver) {
        List<Integer> contains = this.findContains(request);
        List<Integer> valuesList = request.getValuesList();
        valuesList.removeAll(contains);
        responseObserver.onNext(CacheStore.CacheStoreFilterRes.newBuilder().addAllValues(valuesList).build());
        responseObserver.onCompleted();
    }

    private List<Integer> findContains(CacheStore.CacheStoreFilterReq request) {
        List<Integer> res = new ArrayList<>();
        Common.CacheConfig cacheConfig = request.getCacheConfig();
        if (cacheConfig.getCacheType() == Common.CacheType.Full) {
            res.addAll(fullRelationCache.findContains(request.getCacheKey(), request.getValuesList()));
        } else {
            res.addAll(limitRelationCache.findContains(request.getCacheKey(), request.getValuesList(), cacheConfig.getDays()));
        }
        return res;
    }

    @Override
    public void migrating(CacheStore.CacheMigrateReq request, StreamObserver<CacheStore.CacheMigrateRes> responseObserver) {
        slotMigratingMap.put(request.getSlotId(), request.getHostAndPort());
        try {
            CountDownLatch countDownLatch = createImportingClient(request);
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            slotMigratingMap.remove(request.getSlotId());
        }
        responseObserver.onNext(CacheStore.CacheMigrateRes.newBuilder().build());
        responseObserver.onCompleted();
    }


    private CountDownLatch createImportingClient(CacheStore.CacheMigrateReq request) {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        CacheStoreServiceGrpc.CacheStoreServiceStub stub = cacheStoreRpcServiceFactory.createAsyncStub(request.getHostAndPort());
        StreamObserver<CacheStore.CacheImportReq> reqStreamObserver = stub.importing(new StreamObserver<CacheStore.CacheImportRes>() {
            @Override
            public void onNext(CacheStore.CacheImportRes cacheImportRes) {
//                if (Common.CacheType.Full == cacheImportRes.getCacheType()) {
//                    fullRelationCache.remove(request.getSlotId(), cacheImportRes.getCombineKey());
//                }
//                if (Common.CacheType.Limit == cacheImportRes.getCacheType()) {
//                    limitRelationCache.remove(request.getSlotId(), cacheImportRes.getCombineKey());
//                }
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                finishLatch.countDown();
            }
        });
        Map<String, ScaleBloomFilter> bloomFilterMap = fullRelationCache.getCacheBySlot(request.getSlotId());
        bloomFilterMap.forEach((key, bloom) -> reqStreamObserver.onNext(CacheStore.CacheImportReq.newBuilder()
                .setSlotId(request.getSlotId()).setCacheType(Common.CacheType.Full)
                .setCombineKey(key).setBloomSerializable(ByteString.copyFrom(ProtobufUtil.serializer(bloom))).build()));

        LimitQueue limitQueue = limitRelationCache.getCacheBySlot(request.getSlotId());
        Map<String, TreeMap<Long, ScaleBloomFilter>> bloomMap = limitQueue.getBloomMap();
        bloomMap.forEach((key, bloom) -> reqStreamObserver.onNext(CacheStore.CacheImportReq.newBuilder()
                .setSlotId(request.getSlotId()).setCacheType(Common.CacheType.Limit)
                .setCombineKey(key).setBloomSerializable(ByteString.copyFrom(ProtobufUtil.serializer(bloom))).build()));
        reqStreamObserver.onCompleted();
        fullRelationCache.remove(request.getSlotId());
        limitRelationCache.remove(request.getSlotId());
        return finishLatch;
    }

    @Override
    public StreamObserver<CacheStore.CacheImportReq> importing(StreamObserver<CacheStore.CacheImportRes> responseObserver) {
        return new StreamObserver<CacheStore.CacheImportReq>() {
            @Override
            public void onNext(CacheStore.CacheImportReq cacheImportReq) {
                if (Common.CacheType.Full == cacheImportReq.getCacheType()) {
                    ScaleBloomFilter fullCache
                            = ProtobufUtil.deserializer(cacheImportReq.getBloomSerializable().toByteArray(), ScaleBloomFilter.class);
                    fullRelationCache.addTrunk(cacheImportReq.getSlotId(), cacheImportReq.getCombineKey(), fullCache);
                } else if (Common.CacheType.Limit == cacheImportReq.getCacheType()) {
                    TreeMap<Long, ScaleBloomFilter> treeMap = ProtobufUtil.deserializer(cacheImportReq.getBloomSerializable().toByteArray(), TreeMap.class);
                    limitRelationCache.addTrunk(cacheImportReq.getSlotId(), cacheImportReq.getCombineKey(), treeMap);
                }
                responseObserver.onNext(CacheStore.CacheImportRes.newBuilder()
                        .setCacheType(cacheImportReq.getCacheType())
                        .setSlotId(cacheImportReq.getSlotId())
                        .setCombineKey(cacheImportReq.getCombineKey()).build());
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("调用出错:{}",throwable.getMessage());
            }

            /**
             * 客户端写完成
             */
            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void keys(Empty request, StreamObserver<CacheStore.SlotsGetRes> responseObserver) {
        Map<Integer, Integer> limitSlots = limitRelationCache.keys();
        Map<Integer, Integer> fullSlots = fullRelationCache.keys();
        Map<Integer, Integer> result = new HashMap<>();
        limitSlots.forEach((slot, cnt) -> {
            result.put(slot, cnt);
        });
        fullSlots.forEach((slot, cnt) -> {
            if (result.containsKey(slot)) {
                result.put(slot, result.get(slot) + cnt);
            } else {
                result.put(slot, cnt);
            }
        });
        responseObserver.onNext(CacheStore.SlotsGetRes.newBuilder().putAllSlots(result).build());
        responseObserver.onCompleted();
    }
}
