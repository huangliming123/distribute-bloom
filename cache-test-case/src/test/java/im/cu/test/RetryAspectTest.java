package im.cu.test;

import im.cu.api.utils.DelayRetry;
import org.springframework.stereotype.Component;

/**
 * Created by huangliming on 2021/4/15
 */
@Component
public class RetryAspectTest {

    @DelayRetry
    public void test() {
        System.out.println(1/0);
    }
}
