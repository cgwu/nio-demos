package com.dannis.curator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * Created by sam on 17-10-25.
 */
public class RecipesNoLock {
    public static void main(String[] args) {
        CountDownLatch latch = new CountDownLatch(1);
        for (int i = 0; i < 10; i++) {
            new Thread(()->{
                try {
                    latch.await();  // 等所有线程创建并启动完成
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss|SSS");
                String orderNo = sdf.format(new Date());
                System.out.println(Thread.currentThread().getId()+"生成订单号:"+orderNo);

            }).start();
        }
        latch.countDown();
        System.out.println("main done");
    }
}
