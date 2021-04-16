package im.cu.api.grpc.factory;

import im.cu.grpc.api.server.cache.Common;

/**
 * Created by huangliming on 2021/4/2
 */
public class DefaultManagedChannelFactory extends AbstractManagedChannelFactory{

    @Override
    protected Common.HostAndPort getHostAndPortBySlot(int slotId, boolean tmp) {
        return null;
    }
}
