package com.easterfg.takeaway.domain;

import com.easterfg.takeaway.utils.SnowflakeIdWorker;
import org.junit.jupiter.api.Test;

/**
 * @author EasterFG on 2022/10/19
 */
public class SnowflakeTest {

    @Test
    public void test() throws InterruptedException {
        SnowflakeIdWorker worker = new SnowflakeIdWorker(1, 1);
        for (int i = 0; i < 10; i++) {
            System.out.println(worker.nextId());
        }
        Thread.sleep(100);
        System.out.println(worker.nextId());
    }

}
