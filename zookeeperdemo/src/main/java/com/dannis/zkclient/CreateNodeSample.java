package com.dannis.zkclient;

import org.I0Itec.zkclient.ZkClient;

/**
 * Created by sam on 17-10-24.
 */
public class CreateNodeSample {
    public static void main(String[] args) {
        ZkClient zkClient = new ZkClient("localhost:2181", 5000);
        System.out.println("ZooKeeper session established.");
        String path = "/zk-book/c1";
        zkClient.createPersistent(path, true);

//        zkClient.writeData(path,"Hello中国@!");

        /* 若在zkCli.sh中修改节点数据，再读取，报异常 */
//        String data = zkClient.readData(path);
//        System.out.println(data);

        boolean result = zkClient.delete(path);

        System.out.println("done:" + result);
    }
}
