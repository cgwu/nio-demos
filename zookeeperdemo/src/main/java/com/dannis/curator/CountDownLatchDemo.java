package com.dannis.curator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * Created by sam on 17-10-25.
 * 参考: http://www.iteye.com/topic/1002652
 */
public class CountDownLatchDemo {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss|SSS");

    public static void main(String[] args) {
        CountDownLatch latch = new CountDownLatch(2);   // 2人协同工作
        Worker w1= new Worker("Zhansang", 5000, latch);
        Worker w2= new Worker("Lisi", 8000, latch);
        w1.start();
        w2.start();
        System.out.println("main before await");
        try {
            latch.await();  //等所有线程完成工作
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("main after await");
    }

    static class Worker extends Thread {
        String workName;
        int workTime;
        CountDownLatch latch;

        public Worker(String workName, int workTime, CountDownLatch latch){
            this.workName = workName;
            this.workTime = workTime;
            this.latch = latch;
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getId() + " begin work: " +workName +" at " +sdf.format(new Date()));
            doWork();
            System.out.println(Thread.currentThread().getId() +" end work: " +workName +" at " +sdf.format(new Date()));
            latch.countDown();  // !!完成工作，计数器减一!!
        }

        private void doWork(){
            try {
                Thread.sleep(this.workTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
