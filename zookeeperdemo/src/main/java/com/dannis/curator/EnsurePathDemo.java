package com.dannis.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.EnsurePath;

/**
 * Created by sam on 17-10-26.
 */
public class EnsurePathDemo {
    static String path = "/zk-book/c1";

    static CuratorFramework client = CuratorFrameworkFactory.builder()
            .connectString("localhost:2181")
            .sessionTimeoutMs(5000)
            .retryPolicy(new ExponentialBackoffRetry(1000,3))
            .build();

    public static void main(String[] args) {
        client.start();
        client.usingNamespace("zk-book");
        EnsurePath ensurePath = new EnsurePath(path);
        try {
            ensurePath.ensure(client.getZookeeperClient());
            ensurePath.ensure(client.getZookeeperClient());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
