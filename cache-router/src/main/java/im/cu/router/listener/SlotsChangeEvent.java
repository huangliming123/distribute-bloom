package im.cu.router.listener;

import im.cu.grpc.api.server.cache.Common;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * Created by huangliming on 2021/3/18
 */
public class SlotsChangeEvent extends ApplicationEvent {

    private Map<Integer, Common.HostAndPort> routerTab;

    public SlotsChangeEvent(Object source) {
        super(source);
    }

    public SlotsChangeEvent(Object source, Map<Integer, Common.HostAndPort> routerTab) {
        super(source);
        this.routerTab = routerTab;
    }

    public Map<Integer, Common.HostAndPort> getRouterTab() {
        return routerTab;
    }
}
