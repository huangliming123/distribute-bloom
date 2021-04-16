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
public class LimitCacheDTO implements Serializable {

    private static final long serialVersionUID = -1474981503765265805L;
    private Map<Integer, LimitQueue> cache;
    private Common.CacheType cacheType;
}
