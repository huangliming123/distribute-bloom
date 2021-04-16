package im.cu.proxy.config;

import im.cu.proxy.server.thrift.ThriftServerLauncher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by huangliming on 2021/4/2
 */
@Configuration
public class ThriftServerConfiguration {

    @Bean
    public ThriftServerLauncher thriftServerLauncher() {
        return new ThriftServerLauncher();
    }


}
