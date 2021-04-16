package im.cu.router.dto;

import im.cu.grpc.api.server.cache.Common;
import lombok.Data;

/**
 * Created by huangliming on 2021/4/2
 */
@Data
public class SlotsShardReq {

    private String slotParts;

    private HostAndPortJson target;

    @Data
    public class HostAndPortJson {
        private String host;
        private int port;
        public Common.HostAndPort convert() {
            return Common.HostAndPort.newBuilder()
                    .setHost(host).setPort(port).build();
        }
    }
}
