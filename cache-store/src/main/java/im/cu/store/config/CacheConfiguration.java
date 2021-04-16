package im.cu.store.config;

import im.cu.api.utils.DumpService;
import im.cu.api.utils.DumpServiceConfiguration;
import im.cu.store.cache.FullRelationCache;
import im.cu.store.cache.FullRelationCacheImpl;
import im.cu.store.cache.LimitRelationCache;
import im.cu.store.cache.LimitRelationCacheImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Created by huangliming on 2021/3/12
 */
@Configuration
public class CacheConfiguration {

    @Bean
    public FullRelationCache fullRelationCache(DumpService dumpService) {
        return new FullRelationCacheImpl(3000, 0.03, dumpService);
    }

    @Bean
    public LimitRelationCache limitRelationCache(DumpService dumpService) {
        return new LimitRelationCacheImpl(31, 3000, 0.03, dumpService);
    }

}
