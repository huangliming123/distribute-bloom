package im.cu.api.utils;

import org.springframework.stereotype.Indexed;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by huangliming on 2021/4/15
 * @author huangliming
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Indexed
public @interface DelayRetry {

    /**
     * 首次执行延迟时间
     * @return
     */
    int initialDelay() default 30;

    /**
     * 重试最大次数
     * @return
     */
    int maxTimes() default 3;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    Class<?> responseType() default Object.class;
}
