package im.cu.api;

import im.cu.grpc.api.server.cache.Common;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by huangliming on 2021/4/14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Plan {

    private int slot;
    private Common.HostAndPort to;
}

