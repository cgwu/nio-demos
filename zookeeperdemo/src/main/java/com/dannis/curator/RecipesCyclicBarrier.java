package com.dannis.curator;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by sam on 17-10-26.
 */
public class RecipesCyclicBarrier {
    public static void main(String[] args) {
        CyclicBarrier barrier =  new CyclicBarrier(3);
        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 3; i++) {
            executor.submit(()->{
                System.out.println("选手" + Thread.currentThread().getId() +"准备好了");
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
                System.out.println("选手"+Thread.currentThread().getId() +"开跑");
            });
        }
        System.out.println("main done");
        executor.shutdown();
    }
}
