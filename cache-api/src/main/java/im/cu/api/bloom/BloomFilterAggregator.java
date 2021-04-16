package im.cu.api.bloom;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by huangliming on 2021/4/2
 */
@Data
public class BloomFilterAggregator implements Serializable {

    private static final long serialVersionUID = -1704154333831962098L;
    private FullCacheDTO fullCacheDTO;
    private LimitCacheDTO limitCacheDTO;
}

