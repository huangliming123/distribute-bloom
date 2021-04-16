package im.cu.api.register;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by huangliming on 2021/4/7
 */
@Configuration
public class ZkConfiguration {

    @Value("${zookeeper.server}")
    private String zookeeperServer;
    @Value(("${zookeeper.sessionTimeoutMs}"))
    private int sessionTimeoutMs;
    @Value("${zookeeper.connectionTimeoutMs}")
    private int connectionTimeoutMs;
    @Value("${zookeeper.maxRetries}")
    private int maxRetries;
    @Value("${zookeeper.baseSleepTimeMs}")
    private int baseSleepTimeMs;

    @Value("${spring.application.name}")
    private String applicationName;
    @Value("${grpc.server.port}")
    private int grpcPort;


    @Bean(initMethod = "init", destroyMethod = "stop")
    public ZkClient zkClient() {
        ZkClient zkClient = new ZkClient();
        zkClient.setZookeeperServer(zookeeperServer);
        zkClient.setSessionTimeoutMs(sessionTimeoutMs);
        zkClient.setConnectionTimeoutMs(connectionTimeoutMs);
        zkClient.setMaxRetries(maxRetries);
        zkClient.setBaseSleepTimeMs(baseSleepTimeMs);
        return zkClient;
    }

    @Bean
    public ZookeeperRegister zookeeperRegister(ZkClient zkClient) throws UnknownHostException {
        return new ZookeeperRegister(zkClient, ZKConstants.CACHE_ROOT_PATH, applicationName
                , InetAddress.getLocalHost().getHostAddress() + ":" + grpcPort);
    }
}
