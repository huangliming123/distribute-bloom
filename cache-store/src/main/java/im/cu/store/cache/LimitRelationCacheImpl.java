package im.cu.store.cache;

import im.cu.api.bloom.LimitQueue;
import im.cu.api.bloom.ScaleBloomFilter;
import im.cu.api.utils.DumpService;
import im.cu.grpc.api.server.cache.Common;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
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
 * 按照天数限制缓存模板
 */
@Slf4j
public class LimitRelationCacheImpl implements LimitRelationCache {

    /**
     * 缓存保留天数
     */
    protected final int maxDays;

    /**
     * bit数组长度
     */
    protected final int expectedInsertions;

    /**
     * 失误率
     */
    protected final double fpp;

    /**
     * limitCache
     */
    private Map<Integer, LimitQueue> slotMap;

    private static final int DUMP_PERIOD = 15;

    private DumpService dumpService;

    private ScheduledExecutorService scheduledExecutorService;

    public LimitRelationCacheImpl(int maxDays, int expectedInsertions, double fpp, DumpService dumpService) {
        this.dumpService = dumpService;
        this.maxDays = maxDays;
        this.expectedInsertions = expectedInsertions;
        this.fpp = fpp;
        slotMap = new ConcurrentHashMap<>();
        Object data = dumpService.read(StringUtils.uncapitalize(getClass().getSimpleName()));
        if (!ObjectUtils.isEmpty(data)) {
            slotMap = (Map<Integer, LimitQueue>) data;
        }
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                dump();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }, 1, DUMP_PERIOD, TimeUnit.MINUTES);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> dump()));
    }

    @Override
    public List<Integer> findContains(Common.CacheKey key, Collection<Integer> values, int days) {
        int slotId = getSlotId(key);
        String combineKey = getCombineKey(key);
        LimitQueue limitQueue = slotMap.get(slotId);
        if (limitQueue == null) {
            return Collections.emptyList();
        }
        Collection<ScaleBloomFilter> bloomFilters = limitQueue.getFrom(combineKey, getPreDay(days));
        if (CollectionUtils.isEmpty(bloomFilters)) {
            return Collections.emptyList();
        }
        List<Integer> res = new ArrayList<>();
        for (ScaleBloomFilter bloomFilter : bloomFilters) {
            for (Integer value : values) {
                if (bloomFilter.mightContain(value)) {
                    res.add(value);
                }
            }
        }
        return res;
    }

    @Override
    public void add(Common.CacheKey key, int value) {
        this.add(key, value, System.currentTimeMillis());
    }

    @Override
    public void add(Common.CacheKey key, int value, long timeMillis) {
        int slotId = getSlotId(key);
        String combineKey = getCombineKey(key);
        LimitQueue limitQueue = slotMap.computeIfAbsent(slotId, k -> new LimitQueue(maxDays, expectedInsertions, fpp));
        limitQueue.add(combineKey, value, timeMillis);
    }

    @Override
    public LimitQueue getCacheBySlot(int slotId) {
        return Optional.ofNullable(slotMap.get(slotId)).orElse(new LimitQueue(maxDays, expectedInsertions, fpp));
    }

    @Override
    public void addTrunk(int slotId, String combineKey, TreeMap<Long, ScaleBloomFilter> treeMap) {
        if (CollectionUtils.isEmpty(treeMap)) {
            return;
        }
        LimitQueue limitQueue = slotMap.computeIfAbsent(slotId, s -> new LimitQueue(maxDays, expectedInsertions, fpp));
        limitQueue.put(combineKey, treeMap);
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
        Iterator<Map.Entry<Integer, LimitQueue>> iterator = slotMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, LimitQueue> entry = iterator.next();
            if (!slots.contains(entry.getKey())) {
                iterator.remove();
            }
        }
    }

    public void cleanLimitQueue() {
        slotMap.forEach((slot, limitQueue) -> {
            limitQueue.clean();
        });
    }

    @Override
    public void remove(int slotId) {
        slotMap.remove(slotId);
    }

    @Override
    public void remove(int slotId, String combineKey) {
        LimitQueue limitQueue = slotMap.get(slotId);
        if (limitQueue != null) {
            limitQueue.remove(combineKey);
        }
        if (limitQueue.isEmpty()) {
            slotMap.remove(slotId);
        }
    }

    protected long getPreDay(int days) {
        Date date = DateUtils.addDays(new Date(), -days);
        return DateUtils.truncate(date, Calendar.DAY_OF_MONTH).getTime();
    }
}
