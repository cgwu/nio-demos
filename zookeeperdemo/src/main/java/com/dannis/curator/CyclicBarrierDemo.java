package com.dannis.curator;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by sam on 17-10-25.
 * ref: http://www.cnblogs.com/dolphin0520/p/3920397.html
 */
public class CyclicBarrierDemo {
    public static void main(String[] args) {
        int N = 4;
        CyclicBarrier barrier = new CyclicBarrier(N, ()->{
            System.out.println("当前线程"+Thread.currentThread().getName());
        });
        for(int i=0;i<4;i++){
            new Writer(barrier).start();
        }
        System.out.println("main done");
    }

    static class Writer extends Thread {
        private CyclicBarrier cyclicBarrier;
        public Writer(CyclicBarrier cyclicBarrier){
            this.cyclicBarrier = cyclicBarrier;
        }

        @Override
        public void run() {
            System.out.println("线程"+Thread.currentThread().getName()+"正在写入数据...");
            try {
                Thread.sleep(5000);      //以睡眠来模拟写入数据操作
                System.out.println("线程"+Thread.currentThread().getName()+"写入数据完毕，等待其他线程写入完毕");
                cyclicBarrier.await();  //!! 等待其它线程完成
            } catch (InterruptedException e) {
                e.printStackTrace();
            }catch(BrokenBarrierException e){
                e.printStackTrace();
            }
            System.out.println("线程"+Thread.currentThread().getName()+":所有线程写入完毕，继续处理其他任务...");
        }
    }

}
