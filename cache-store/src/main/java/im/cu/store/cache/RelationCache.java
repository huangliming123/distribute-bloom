package im.cu.store.cache;

import im.cu.grpc.api.server.cache.Common;

import java.util.Map;
import java.util.Set;

/**
 * Created by huangliming on 2021/3/23
 */
public interface RelationCache {

    void add(Common.CacheKey key, int value);

    void add(Common.CacheKey key, int value, long date);

    void remove(int slotId);

    void remove(int slotId, String combineKey);

    Map<Integer, Integer> keys();

    default int getSlotId(Common.CacheKey key) {
        return key.getUserId() % 1024;
    }

    default String getCombineKey(Common.CacheKey key) {
        return key.getPrefix() + ":" + key.getUserId();
    }

    void dump();

    void cleanSlot(Set<Integer> slots);
}
