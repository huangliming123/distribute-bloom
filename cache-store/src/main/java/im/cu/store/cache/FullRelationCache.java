package im.cu.store.cache;

import im.cu.api.bloom.ScaleBloomFilter;
import im.cu.grpc.api.server.cache.Common;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Created by huangliming on 2021/3/22
 */
public interface FullRelationCache extends RelationCache {

    List<Integer> findContains(Common.CacheKey key, Collection<Integer> targetUserIds);

    default List<Integer> findContainsRetain(Collection<Common.CacheKey> cacheKeys, Collection<Integer> targetUserIds) {
        List<Integer> tmp = null;
        for (Common.CacheKey cacheKey : cacheKeys) {
            List<Integer> contains = this.findContains(cacheKey, targetUserIds);
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

    Map<String, ScaleBloomFilter> getCacheBySlot(int slotId);

    void addTrunk(int slotId, String combineKey, ScaleBloomFilter fullCache);
}
