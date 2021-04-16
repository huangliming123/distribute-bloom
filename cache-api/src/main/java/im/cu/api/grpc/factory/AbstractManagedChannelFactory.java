package im.cu.api.grpc.factory;

import im.cu.grpc.api.server.cache.Common;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by huangliming on 2021/4/1
 */
@Slf4j
public abstract class AbstractManagedChannelFactory {

    private Map<Common.HostAndPort, ManagedChannel> channelMap = new ConcurrentHashMap<>();

    public ManagedChannel create(Common.HostAndPort hostAndPort) {
        return channelMap.computeIfAbsent(hostAndPort, hp ->
                ManagedChannelBuilder.forAddress(hostAndPort.getHost(), hostAndPort.getPort()).usePlaintext().build());
    }

    public ManagedChannel create(int slotId) {
        return this.create(slotId, false);
    }

    public ManagedChannel create(int slotId, boolean tmp) {
        Common.HostAndPort hostAndPort = getHostAndPortBySlot(slotId, tmp);
        if (hostAndPort == null) {
            log.warn("slotId:{}, unInitialized ", slotId);
        }
        return this.create(hostAndPort);
    }

    protected abstract Common.HostAndPort getHostAndPortBySlot(int slotId, boolean tmp);
}
