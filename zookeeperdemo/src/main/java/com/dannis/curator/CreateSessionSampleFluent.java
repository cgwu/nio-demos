package com.dannis.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

/**
 * Created by sam on 17-10-25.
 */
public class CreateSessionSampleFluent {
    public static void main(String[] args) throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory
                .builder()
                .connectString("localhost:2181")
                .sessionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .namespace("base")      //所有操作都是基于这个前缀: /base
                .build();
        client.start();

//        client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/foo/bar","init初始".getBytes());
//        client.setData().forPath("/foo/bar","init初始2".getBytes());

        Stat stat = new Stat();
        String data = new String(client.getData().storingStatIn(stat).forPath("/foo/bar"));
        System.out.println(stat.getVersion() + ":" + data);

        client.setData().withVersion(stat.getVersion()).forPath("/foo/bar", (data+"$").getBytes());

        try {
            //上面更新过一次，版本已经加1，按旧版本再次更新将出错。
            client.setData().withVersion(stat.getVersion()).forPath("/foo/bar", (data+"$$").getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        client.close();
        System.out.println("done");
    }
}
