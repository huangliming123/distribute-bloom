package im.cu.store.cache;

import im.cu.api.bloom.LimitQueue;
import im.cu.api.bloom.ScaleBloomFilter;
import im.cu.api.grpc.factory.CacheRouterRpcServiceFactory;
import im.cu.api.register.ZKConstants;
import im.cu.api.register.ZkClient;
import im.cu.api.utils.DumpService;
import im.cu.grpc.api.server.cache.Common;
import im.cu.grpc.api.server.cache.router.CacheRouter;
import im.cu.grpc.api.server.cache.router.CacheRouterServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by huangliming on 2021/3/22
 * 全量缓存模板
 */
@Slf4j
public class FullRelationCacheImpl implements FullRelationCache {

    protected final int expectedInsertions;
    protected final double fpp;

    private static final int DUMP_PERIOD = 15;

    protected Map<Integer, Map<String, ScaleBloomFilter>> slotMap;

    private DumpService dumpService;

    public FullRelationCacheImpl(int expectedInsertions, double fpp, DumpService dumpService) {
        this.dumpService = dumpService;
        this.expectedInsertions = expectedInsertions;
        this.fpp = fpp;
        Object read = dumpService.read(StringUtils.uncapitalize(getClass().getSimpleName()));
        if (!ObjectUtils.isEmpty(read)) {
            slotMap = (Map<Integer, Map<String, ScaleBloomFilter>>) read;
        }
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            try {
                dump();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }, 1, DUMP_PERIOD, TimeUnit.MINUTES);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> dump()));
    }


    @Override
    public List<Integer> findContains(Common.CacheKey key, Collection<Integer> values) {
        int slotId = getSlotId(key);
        String combineKey = getCombineKey(key);
        Map<String, ScaleBloomFilter> bloomFilterMap = slotMap.computeIfAbsent(slotId, s -> new ConcurrentHashMap<>());
        ScaleBloomFilter bloomFilter = bloomFilterMap.get(combineKey);
        if (bloomFilter == null) {
            return Collections.emptyList();
        } else {
            List<Integer> res = new ArrayList<>();
            for (Integer value : values) {
                if (bloomFilter.mightContain(value)) {
                    res.add(value);
                }
            }
            return res;
        }
    }

    @Override
    public void add(Common.CacheKey key, int value) {
        int slotId = getSlotId(key);
        String combinedKey = getCombineKey(key);
        Map<String, ScaleBloomFilter> bloomFilterMap = slotMap.computeIfAbsent(slotId, s -> new ConcurrentHashMap<>());
        ScaleBloomFilter bloomFilter = bloomFilterMap.computeIfAbsent(combinedKey, k -> new ScaleBloomFilter(expectedInsertions, fpp));
        bloomFilter.put(value);
    }

    @Override
    public void add(Common.CacheKey key, int value, long date) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, ScaleBloomFilter> getCacheBySlot(int slotId) {
        return Optional.ofNullable(slotMap.get(slotId)).orElse(new HashMap<>());
    }

    @Override
    public void addTrunk(int slotId, String combineKey, ScaleBloomFilter fullCache) {
        if (fullCache == null) {
            return;
        }
        Map<String, ScaleBloomFilter> stringScaleBloomFilterMap = slotMap.computeIfAbsent(slotId, s -> new ConcurrentHashMap<>());
        stringScaleBloomFilterMap.put(combineKey, fullCache);
    }

    @Override
    public Map<Integer, Integer> keys() {
        Map<Integer, Integer> result = new HashMap<>();
        this.slotMap.forEach((k, v) -> {
            result.put(k, v.size());
        });
        return result;
    }

    @Override
    public void dump() {
        dumpService.dump(slotMap, StringUtils.uncapitalize(getClass().getSimpleName()));
    }

    @Override
    public void cleanSlot(Set<Integer> slots) {
        Iterator<Map.Entry<Integer, Map<String, ScaleBloomFilter>>> iterator = slotMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Map<String, ScaleBloomFilter>> entry = iterator.next();
            if (!slots.contains(entry.getKey())) {
                iterator.remove();
            }
        }
    }

    @Override
    public void remove(int slotId) {
        slotMap.remove(slotId);
    }

    @Override
    public void remove(int slotId, String combineKey) {
        Map<String, ScaleBloomFilter> map = slotMap.get(slotId);
        if (map != null) {
            map.remove(combineKey);
        }
        if (CollectionUtils.isEmpty(map)) {
            slotMap.remove(slotId);
        }
    }
}
