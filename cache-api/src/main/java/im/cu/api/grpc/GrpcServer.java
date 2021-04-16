package im.cu.api.grpc;

import java.lang.annotation.*;

/**
 * Created by huangliming on 2021/4/1
 */
@Target(ElementType.TYPE)
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface GrpcServer {
}
