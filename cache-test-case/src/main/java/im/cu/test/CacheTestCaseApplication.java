package im.cu.test;

import im.cu.api.grpc.GrpcServerConfiguration;
import im.cu.api.monitor.MeterRegistryConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {MeterRegistryConfiguration.class, GrpcServerConfiguration.class})
public class CacheTestCaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(CacheTestCaseApplication.class, args);
    }

}
