package im.cu.proxy.server.thrift;

import im.cu.api.match_smart_cache.thrift.CacheProxyService;
import im.cu.api.register.ZKConstants;
import im.cu.api.register.ZkClient;
import im.cu.rpc.thrift.server.BaseThriftServer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TServerTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.net.InetAddress;

/**
 * Created by huangliming on 2021/4/2
 */
@Slf4j
public class ThriftServerLauncher implements ApplicationListener<ContextRefreshedEvent> {


    private class ThriftServer extends BaseThriftServer {
        public ThriftServer(String host, int port, TProcessor processor, String registerRootPath, String serviceKey) throws Exception {
            super(host, port, processor, registerRootPath, serviceKey);
        }
    }

    @Value("${thrift.server.port}")
    private int port;

    @Autowired
    private ThriftCacheProxyServer thriftCacheProxyServer;

    @Autowired
    private ZkClient zkClient;

    private TServerTransport serverTransport;

    private TServer server;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @SneakyThrows
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        TProcessor tProcessor = new CacheProxyService.Processor<CacheProxyService.Iface>(thriftCacheProxyServer);
        ThriftServer thriftServer = new ThriftServer(
                InetAddress.getLocalHost().getHostAddress(), port, tProcessor, ZKConstants.THRIFT_ROOT_PATH, ZKConstants.THRIFT_SERVER_KEY
        );
        Runtime.getRuntime().addShutdownHook(new Thread(() -> stop()));
    }

    /**
     * 服务Stop方法
     */
    private void stop() {
        if (server != null) {
            server.stop();
        }
    }
}
