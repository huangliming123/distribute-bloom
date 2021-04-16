package im.cu.api.grpc;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Map;

/**
 * Created by huangliming on 2021/4/1
 */
public class GrpcServerLauncher implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

    @Value("${grpc.server.port}")
    private int port;

    private ApplicationContext applicationContext;

    private Server server;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        new Thread(() -> {
            Map<String, Object> serverList = applicationContext.getBeansWithAnnotation(GrpcServer.class);
            if (CollectionUtils.isEmpty(serverList)) {
                return;
            }
            try{
                ServerBuilder serverBuilder = ServerBuilder.forPort(port);
                for (Object bean : serverList.values()){
                    serverBuilder.addService((BindableService) bean);
                    logger.info(bean.getClass().getSimpleName() + " is starting");
                }
                server = serverBuilder.build().start();
                logger.info("grpc server is started at " +  port);
                server.awaitTermination();
                Runtime.getRuntime().addShutdownHook(new Thread(()-> grpcStop()));
            } catch (IOException e){
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * GRPC 服务Stop方法
     */
    private void grpcStop(){
        if (server != null){
            server.shutdownNow();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
