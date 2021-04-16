package im.cu.proxy.server;

import im.cu.api.grpc.factory.AbstractManagedChannelFactory;
import im.cu.grpc.api.server.cache.Common;
import im.cu.proxy.RouterCache;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by huangliming on 2021/4/2
 */
public class ShardManagedChannelFactory extends AbstractManagedChannelFactory {

    @Autowired
    private RouterCache routerCache;

    @Override
    protected Common.HostAndPort getHostAndPortBySlot(int slotId, boolean tmp) {
        return tmp ? routerCache.getTmpBySlot(slotId) : routerCache.getBySlot(slotId);
    }
}
