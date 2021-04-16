package im.cu.api.bloom;

import org.apache.commons.lang3.time.DateUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by huangliming on 2021/3/22
 */
public class LimitQueue implements Serializable {

    private static final long serialVersionUID = -4961508596110264000L;
    private Map<String, TreeMap<Long, ScaleBloomFilter>> map;
    private int limit;
    private int expectedInsertions;
    private double fpp;

    public LimitQueue(int limit, int expectedInsertions, double fpp) {
        this.map = new ConcurrentHashMap<>();
        this.limit = limit;
        this.expectedInsertions = expectedInsertions;
        this.fpp = fpp;
    }

    public void add(String key, int value) {
        add(key, value, System.currentTimeMillis());
    }

    public void add(String key, int value, long timeMillis) {
        TreeMap<Long, ScaleBloomFilter> treeMap = map.get(key);
        long currentDay = getDay(timeMillis);
        if (treeMap == null) {
            treeMap = new TreeMap<>();
            ScaleBloomFilter scaleBloomFilter = new ScaleBloomFilter(expectedInsertions, fpp);
            treeMap.put(currentDay, scaleBloomFilter);
            map.put(key, treeMap);
        } else {
            if (currentDay > treeMap.lastKey()) {
                if (!DateUtils.addDays(new Date(treeMap.firstKey()), limit - 1).after(new Date(treeMap.lastKey()))) {
                    treeMap.remove(treeMap.firstKey());
                }
                ScaleBloomFilter bloomFilter = new ScaleBloomFilter(expectedInsertions, fpp);
                bloomFilter.put(value);
                treeMap.put(currentDay, bloomFilter);
            } else {
                ScaleBloomFilter bloomFilter = treeMap.lastEntry().getValue();
                bloomFilter.put(value);
            }
        }
    }

    public void put(String key, TreeMap<Long, ScaleBloomFilter> treeMap) {
        map.put(key, treeMap);
    }

    public Collection<ScaleBloomFilter> getFrom(String key, long from) {
        TreeMap<Long, ScaleBloomFilter> treeMap = map.get(key);
        SortedMap<Long, ScaleBloomFilter> longBloomFilterSortedMap = treeMap.tailMap(from);
        return longBloomFilterSortedMap == null ? Collections.emptyList() : longBloomFilterSortedMap.values();
    }

    public void remove(String key) {
        map.remove(key);
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map == null || map.size() == 0;
    }

    private long getDay(long timeMillis) {
        Date truncate = DateUtils.truncate(new Date(timeMillis), Calendar.DAY_OF_MONTH);
        return truncate.getTime();
    }

    public Map<String, TreeMap<Long, ScaleBloomFilter>> getBloomMap() {
        return this.map;
    }

    public void clean() {
        map.forEach((key, tree) -> {
            while (tree != null
                    && !tree.isEmpty()
                    && !DateUtils.addDays(new Date(tree.firstKey()), limit - 1).after(new Date(tree.lastKey()))) {
                tree.remove(tree.firstKey());
            }
        });
    }
}
