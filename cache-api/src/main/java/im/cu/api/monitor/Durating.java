package im.cu.api.monitor;

import org.springframework.stereotype.Indexed;

import java.lang.annotation.*;

/**
 * Created by huangliming on 2021/4/9
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Indexed
public @interface Durating {

    String name() default "";
}
