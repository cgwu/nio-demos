package io.vertx.starter;

import io.vertx.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start() {
    System.out.println("当前主线程: " + Thread.currentThread().getName());
    vertx.createHttpServer()
      .requestHandler(req -> req.response()
        .putHeader("Content-Type", "text/html;charset=utf-8")
        .end("Hello Vert.x! 你好，Vert.x! from: "
          + Thread.currentThread().getName()))
      .listen(8089);
  }
}
