package com.dannis.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * Created by sam on 17-10-25.
 * 使用Curator实现分布式锁功能
 */
public class RecipesLock {
    static String lockPath = "/curator_recipes_lock_path";
    static CuratorFramework client = CuratorFrameworkFactory.builder()
            .connectString("localhost:2181")
            .retryPolicy(new ExponentialBackoffRetry(1000,3)).build();

    public static void main(String[] args) throws Exception {
        client.start();
        final InterProcessMutex mutex =new InterProcessMutex(client, lockPath);
        CountDownLatch latch = new CountDownLatch(1);
        for (int i = 0; i < 10; i++) {
            new Thread(()->{
                try {
                    latch.await();  // 等所有线程创建并启动完成
                    mutex.acquire();    // 互斥量锁定
                } catch (Exception e) {
                    e.printStackTrace();
                }
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss|SSS");
                String orderNo = sdf.format(new Date());
                System.out.println(Thread.currentThread().getId()+"生成订单号:"+orderNo);
                try {
                    mutex.release();    // 互斥量释放
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
        latch.countDown();
        System.out.println("main done");
    }
}
