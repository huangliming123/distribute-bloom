package im.cu.api.grpc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by huangliming on 2021/4/1
 */
@Configuration
public class GrpcServerConfiguration {

    @Bean
    public GrpcServerLauncher grpcServerLauncher() {
        return new GrpcServerLauncher();
    }
}
