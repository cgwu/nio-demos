package io.vertx.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;

public class MyFirstRXVerticle extends AbstractVerticle {
  @Override
  public void start() throws Exception {
    HttpServer server = vertx.createHttpServer();
  }
}
