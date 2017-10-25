package com.dannis.zkclient;

import org.I0Itec.zkclient.ZkClient;

/**
 * Created by sam on 17-10-24.
 */
public class CreateSessionSample {
    public static void main(String[] args) {
        ZkClient zkClient = new ZkClient("localhost:2181",5000);
        System.out.println("ZooKeeper session established.");
    }
}
