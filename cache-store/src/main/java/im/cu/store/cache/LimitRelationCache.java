package im.cu.store.cache;

import im.cu.api.bloom.LimitQueue;
import im.cu.api.bloom.ScaleBloomFilter;
import im.cu.grpc.api.server.cache.Common;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Created by huangliming on 2021/3/22
 */
public interface LimitRelationCache extends RelationCache {

    List<Integer> findContains(Common.CacheKey key, Collection<Integer> values, int days);

    default List<Integer> findContainsRetain(Collection<Common.CacheKey> cacheKeys, Collection<Integer> targetUserIds, int days) {
        List<Integer> tmp = null;
        for (Common.CacheKey cacheKey : cacheKeys) {
            List<Integer> contains = this.findContains(cacheKey, targetUserIds, days);
            if (tmp == null) {
                tmp = new ArrayList<>(contains);
            }
            tmp.retainAll(contains);
            if (CollectionUtils.isEmpty(tmp)) {
                return Collections.emptyList();
            }
        }
        return tmp;
    }

    LimitQueue getCacheBySlot(int slotId);

    void addTrunk(int slotId, String combineKey, TreeMap<Long, ScaleBloomFilter> treeMap);
}
