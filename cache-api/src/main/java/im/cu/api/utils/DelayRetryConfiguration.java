package im.cu.api.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by huangliming on 2021/4/15
 */
@Configuration
public class DelayRetryConfiguration {

    @Bean
    public RetryAspect retryAspect() {
        return new RetryAspect();
    }
}
