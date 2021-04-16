package im.cu.api.bloom;

import im.cu.grpc.api.server.cache.Common;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by huangliming on 2021/4/2
 */
@Data
@AllArgsConstructor
public class FullCacheDTO implements Serializable {

    private static final long serialVersionUID = 7688036911620555140L;
    private Map<Integer, Map<String, ScaleBloomFilter>> cache;
    private Common.CacheType cacheType;
}
