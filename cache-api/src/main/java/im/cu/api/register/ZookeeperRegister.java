package im.cu.api.register;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Created by huangliming on 2021/4/7
 */
public class ZookeeperRegister implements ApplicationListener<ContextRefreshedEvent> {

    private ZkClient zkClient;
    private String rootPath;
    private String serviceInstance;
    private String serverAddr;

    public ZookeeperRegister(ZkClient zkClient, String rootPath, String serviceInstance, String serverAddr) {
        this.zkClient = zkClient;
        this.rootPath = rootPath;
        this.serviceInstance = serviceInstance;
        this.serverAddr = serverAddr;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        zkClient.register(rootPath, serviceInstance, serverAddr);
    }
}
