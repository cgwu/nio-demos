package com.dannis.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * Created by sam on 17-10-25.
 * 使用Curator实现分布式计数器
 */
public class RecipesDistAtomicInt {
    static String distatomicint_path = "/curator_recipes_distatomicint_path";
    static CuratorFramework client = CuratorFrameworkFactory.builder()
            .connectString("localhost:2181")
            .retryPolicy(new ExponentialBackoffRetry(1000,3)).build();

    public static void main(String[] args) throws Exception {
        client.start();

        DistributedAtomicInteger atomicInteger = new DistributedAtomicInteger(
                client,distatomicint_path, new RetryNTimes(4,100));
        AtomicValue<Integer> rc = atomicInteger.add(8);
        System.out.println("Result: "+rc.succeeded());
        System.out.println("preValue: "+rc.preValue());
        System.out.println("postValue: "+rc.postValue());

        for (int i = 0; i < 20; i++) {
            new Thread(() -> {
                try {
                    AtomicValue<Integer> avi = atomicInteger.add(1);
                    System.out.printf("%s: result: %b prev:%d, post:%d\n",Thread.currentThread().getName(),
                            avi.succeeded(), avi.preValue(),avi.postValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
        System.out.println("main done");
    }
}
