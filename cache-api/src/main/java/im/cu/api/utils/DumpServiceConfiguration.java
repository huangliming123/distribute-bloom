package im.cu.api.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by huangliming on 2021/4/12
 */
@Configuration
public class DumpServiceConfiguration {

    @Bean
    public DumpService dumpService() {
        return new DumpService();
    }
}
