package im.cu.api.grpc;

import im.cu.api.grpc.factory.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by huangliming on 2021/4/2
 */
@Configuration
public class GrpcClientConfiguration {

    @Bean
    @ConditionalOnMissingBean(AbstractManagedChannelFactory.class)
    public DefaultManagedChannelFactory managedChannelFactory() {
        return new DefaultManagedChannelFactory();
    }

    @Bean
    @ConditionalOnMissingBean(CacheStoreRpcServiceFactory.class)
    public CacheStoreRpcServiceFactory cacheStoreRpcServiceFactory(AbstractManagedChannelFactory abstractManagedChannelFactory) {
        return new CacheStoreRpcServiceFactory(abstractManagedChannelFactory);
    }

    @Bean
    @ConditionalOnMissingBean(CacheProxyRpcServiceFactory.class)
    public CacheProxyRpcServiceFactory cacheProxyRpcServiceFactory(AbstractManagedChannelFactory abstractManagedChannelFactory) {
        return new CacheProxyRpcServiceFactory(abstractManagedChannelFactory);
    }

    @Bean
    @ConditionalOnMissingBean(CacheRouterRpcServiceFactory.class)
    public CacheRouterRpcServiceFactory cacheRouterRpcServiceFactory(AbstractManagedChannelFactory abstractManagedChannelFactory) {
        return new CacheRouterRpcServiceFactory(abstractManagedChannelFactory);
    }
}
