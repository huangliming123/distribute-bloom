package im.cu.api.utils;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;

/**
 * Created by huangliming on 2021/4/15
 */
@Aspect
@Slf4j
public class RetryAspect {

    private boolean stop = false;

    private DelayQueue<HalfIncrRetry<Object>> offlineQueue;

    private class HalfIncrRetry<T> implements Delayed {
        final T data;
        final long create;
        final long expire;
        final int times;
        final int maxTimes;
        final int delay;
        final TimeUnit timeUnit;
        final ProceedingJoinPoint pjp;

        public HalfIncrRetry(T data, long create, long expire, int maxTimes, int delay, TimeUnit timeUnit, ProceedingJoinPoint pjp) {
            this.data = data;
            this.create = create;
            this.expire = expire;
            this.times = 1;
            this.maxTimes = maxTimes;
            this.delay = delay;
            this.timeUnit = timeUnit;
            this.pjp = pjp;
        }

        public HalfIncrRetry(T data, long create, long expire, int times, int maxTimes, int delay, TimeUnit timeUnit, ProceedingJoinPoint pjp) {
            this.data = data;
            this.create = create;
            this.expire = expire;
            this.times = times;
            this.maxTimes = maxTimes;
            this.delay = delay;
            this.timeUnit = timeUnit;
            this.pjp = pjp;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(this.expire - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return (int) (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
        }
    }

    @PostConstruct
    private void init() {
        offlineQueue = new DelayQueue<>();
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(() -> {
            while (!stop) {
                HalfIncrRetry halfIncrRetry = null;
                try {
                    halfIncrRetry = offlineQueue.take();
                    halfIncrRetry.pjp.proceed();
                    log.info("offlineQueue剩余任务数:{}", offlineQueue.size());
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                } catch (Throwable throwable) {
                    log.error(throwable.getMessage(), throwable);
                    if (halfIncrRetry != null && halfIncrRetry.times < halfIncrRetry.maxTimes) {
                        offlineQueue.add(new HalfIncrRetry<>(halfIncrRetry.data, System.currentTimeMillis()
                                , System.currentTimeMillis() + (halfIncrRetry.times + 1) * halfIncrRetry.timeUnit.toMillis(halfIncrRetry.delay)
                                , halfIncrRetry.maxTimes, halfIncrRetry.times + 1, halfIncrRetry.delay, halfIncrRetry.timeUnit, halfIncrRetry.pjp));
                    }
                }
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stop = true;
            log.info("shutdownHook.. offlineQueue队列任务数:{}", offlineQueue.size());
            offlineQueue.forEach(retry -> {
                try {
                    retry.pjp.proceed();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        }));
    }

    @Pointcut("@annotation(im.cu.api.utils.DelayRetry)")
    public void retryPoint() {
    }

    @Around("retryPoint()")
    public Object retryAround(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object proceed = joinPoint.proceed();
            return proceed;
        } catch (Exception e) {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            DelayRetry annotation = methodSignature.getMethod().getAnnotation(DelayRetry.class);
            int maxTimes = annotation.maxTimes();
            int delay = annotation.initialDelay();
            TimeUnit timeUnit = annotation.timeUnit();
            offlineQueue.add(new HalfIncrRetry<>(joinPoint.getArgs(), System.currentTimeMillis()
                    , System.currentTimeMillis() + timeUnit.toMillis(delay), maxTimes, delay, timeUnit, joinPoint));
            Class<?> responseClass = annotation.responseType();
            if (responseClass == Object.class) {
                return null;
            }
            return responseClass.newInstance();
        }
    }
}