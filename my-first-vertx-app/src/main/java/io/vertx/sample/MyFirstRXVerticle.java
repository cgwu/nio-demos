package io.vertx.sample;


import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServer;

public class MyFirstRXVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        HttpServer server = vertx.createHttpServer();
        server.requestStream().toObservable()
                .subscribe(req -> req.response().end("Hello from Rx "
                        + Thread.currentThread().getName()));
        server.rxListen(8090).subscribe();
    }
}
