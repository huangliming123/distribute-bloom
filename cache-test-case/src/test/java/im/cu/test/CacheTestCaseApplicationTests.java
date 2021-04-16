package im.cu.test;

import com.google.protobuf.Empty;
import im.cu.api.grpc.factory.CacheStoreRpcServiceFactory;
import im.cu.api.match_smart_cache.thrift.CacheKey;
import im.cu.api.match_smart_cache.thrift.CacheType;
import im.cu.api.register.ZKConstants;
import im.cu.api.register.ZkClient;
import im.cu.grpc.api.server.cache.Common;
import im.cu.grpc.api.server.cache.store.CacheStore;
import im.cu.grpc.api.server.cache.store.CacheStoreServiceGrpc;
import io.micrometer.core.instrument.Counter;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
class CacheTestCaseApplicationTests {

    @Autowired
    private SmartCacheClient smartCacheClient;

    @Autowired
    private CacheStoreRpcServiceFactory cacheStoreRpcServiceFactory;

    @Autowired
    private ZkClient zkClient;

    @Autowired
    private RetryAspectTest retryAspectTest;


    private static final int SAMPLE_CAPACITY = 10000;

    /**
     * 导入SAMPLE_CAPACITY key
     */
    @Test
    void contextLoads() {
        for (int i=0; i<SAMPLE_CAPACITY; i++) {
            smartCacheClient.add(new CacheKey("aloha", i), CacheType.Full, SAMPLE_CAPACITY - i);
        }
    }

    /**
     * 查询各个节点key总和是否=SAMPLE_CAPACITY
     */
    @Test
    void testCacheStoreKeys() {
        List<String> serviceList = zkClient.getChildren(ZKConstants.CACHE_ROOT_PATH + "/" + ZKConstants.CACHE_STORE);
        int keys = 0;
        for (String s : serviceList) {
            String[] split = s.split(":");
            String host = split[0];
            int port = Integer.valueOf(split[1]);
            CacheStoreServiceGrpc.CacheStoreServiceBlockingStub blockingStub = cacheStoreRpcServiceFactory.createBlockingStub(Common.HostAndPort.newBuilder()
                    .setHost(host).setPort(port).build());
            CacheStore.SlotsGetRes res = blockingStub.keys(Empty.newBuilder().build());
            Map<Integer, Integer> slotsMap = res.getSlotsMap();
            keys += slotsMap.values().stream().mapToInt(item -> item).sum();
        }
        Assert.assertEquals(keys, SAMPLE_CAPACITY);
    }

    @Test
    void testAutoReShard() {
        RestTemplate restTemplate = new RestTemplate();
        Map map = restTemplate.postForObject("http://localhost:8080/router/auto-re-shard", "", Map.class);
        System.out.println(map);
    }

    @Test
    void testOffline() {
        RestTemplate restTemplate = new RestTemplate();
        Map map = restTemplate.postForObject("http://localhost:8080/router/offline", "127.0.0.1:18763", Map.class);
        System.out.println(map);
    }
}
