package im.cu.api.monitor;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by huangliming on 2021/4/9
 */
@Configuration
public class MeterRegistryConfiguration {

    @Value("${spring.application.name}")
    private String application;

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> configurer() {
        return (registry) -> registry.config().commonTags("application", application);
    }

    @Bean
    public MonitorAspect duratingAspect() {
        return new MonitorAspect();
    }
}
