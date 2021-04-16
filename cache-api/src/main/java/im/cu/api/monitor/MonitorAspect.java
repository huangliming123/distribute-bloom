package im.cu.api.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by huangliming on 2021/4/9
 */
@Aspect
public class MonitorAspect {

    @Autowired
    private MeterRegistry registry;

    @Value("${spring.application.name}")
    private String application;

    @Pointcut("@annotation(im.cu.api.monitor.Durating)")
    public void timerPoint() {
    }

    @Around("timerPoint()")
    public Object timerAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = Timer.start(registry);
        try {
            Object proceed = joinPoint.proceed();
            return proceed;
        } finally {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            Durating annotation = methodSignature.getMethod().getAnnotation(Durating.class);
            String name = annotation.name();
            if (StringUtils.isBlank(name)) {
                name = application + "_" + methodSignature.getName();
            }
            sample.stop(registry.timer(name));
        }
    }

    @Pointcut("@annotation(im.cu.api.monitor.Counting)")
    public void counterPoint() {
    }

    @Around("counterPoint()")
    public Object counterAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Counting annotation = methodSignature.getMethod().getAnnotation(Counting.class);
        String name = annotation.name();
        if (StringUtils.isBlank(name)) {
            name = application + "_" + methodSignature.getName();
        }
        Counter counter = registry.counter(name);
        Object proceed = joinPoint.proceed();
        counter.increment();
        return proceed;
    }
}









