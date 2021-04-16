package im.cu.proxy.config;

import im.cu.proxy.server.ShardManagedChannelFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by huangliming on 2021/4/2
 */
@Configuration
public class ManagedChannelFactoryConfiguration {

    @Bean
    public ShardManagedChannelFactory shardManagedChannelFactory() {
        return new ShardManagedChannelFactory();
    }
}
