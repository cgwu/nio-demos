/*
 *  Copyright (c) 2017 Red Hat, Inc. and/or its affiliates.
 *  Copyright (c) 2017 INSA Lyon, CITI Laboratory.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vertx.guides.wiki.http;

import com.github.rjeschke.txtmark.Processor;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.guides.wiki.database.rxjava.WikiDatabaseService;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import io.vertx.rxjava.ext.web.handler.CookieHandler;
import io.vertx.rxjava.ext.web.handler.SessionHandler;
import io.vertx.rxjava.ext.web.handler.StaticHandler;
import io.vertx.rxjava.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.rxjava.ext.web.sstore.LocalSessionStore;
import rx.Observable;

import java.util.Arrays;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
public class HttpServerVerticle extends AbstractVerticle {

    public static final String CONFIG_HTTP_SERVER_PORT = "http.server.port";
    public static final String CONFIG_WIKIDB_QUEUE = "wikidb.queue";

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

    private WikiDatabaseService dbService;

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        String wikiDbQueue = config().getString(CONFIG_WIKIDB_QUEUE, "wikidb.queue");
        dbService = io.vertx.guides.wiki.database.WikiDatabaseService.createProxy(vertx.getDelegate(), wikiDbQueue);

        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);

        router.route().handler(CookieHandler.create());
        router.route().handler(BodyHandler.create());
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

        // tag::sockjs-handler-setup[]
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx); // <1>
        BridgeOptions bridgeOptions = new BridgeOptions()
                // EventBus进来的地址
                .addInboundPermitted(new PermittedOptions().setAddress("wikidb.queue"))  // 数据库接口地址
                .addInboundPermitted(new PermittedOptions().setAddress("app.markdown"))  // <2>
                // EventBus出去的地址,可以使用正则表达式: new PermittedOptions().setAddressRegex(regex)
                .addOutboundPermitted(new PermittedOptions().setAddress("page.sth_happen")) // <3>
                .addOutboundPermitted(new PermittedOptions().setAddress("page.saved")); // <3>
        sockJSHandler.bridge(bridgeOptions); // <4>
        router.route("/eventbus/*").handler(sockJSHandler); // <5>
        // end::sockjs-handler-setup[]

        // tag::eventbus-markdown-consumer[]
        vertx.eventBus().<String>consumer("app.markdown", msg -> {
            String html = Processor.process(msg.body());
            msg.reply(html);
        });
        // end::eventbus-markdown-consumer[]

        router.get("/app/*").handler(StaticHandler.create().setCachingEnabled(false));
        router.get("/").handler(context -> context.reroute("/app/index.html"));
        router.get("/sayhi/:name").handler(this::sayHiHandler);
        router.get("/api/pages").handler(this::apiRoot);
        router.get("/api/pages/:id").handler(this::apiGetPage);
        router.post().handler(BodyHandler.create());
        router.post("/api/pages").handler(this::apiCreatePage);
        router.put().handler(BodyHandler.create());
        router.put("/api/pages/:id").handler(this::apiUpdatePage);
        router.delete("/api/pages/:id").handler(this::apiDeletePage);

        int portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT, 8080);
        server
                .requestHandler(router::accept)
                .rxListen(portNumber)
                .subscribe(s -> {
                    LOGGER.info("HTTP server running on port " + portNumber);
                    startFuture.complete();
                }, t -> {
                    LOGGER.error("Could not start a HTTP server", t);
                    startFuture.fail(t);
                });
    }

    private void apiDeletePage(RoutingContext context) {
        int id = Integer.valueOf(context.request().getParam("id"));
        dbService.rxDeletePage(id).subscribe(
                v -> apiResponse(context, 200, null, null),
                t -> apiFailure(context, t));
    }

    private void apiUpdatePage(RoutingContext context) {
        int id = Integer.valueOf(context.request().getParam("id"));
        JsonObject page = context.getBodyAsJson();
        if (!validateJsonPageDocument(context, page, "client", "markdown")) {
            return;
        }
        // tag::publish-on-page-updated[]
        dbService.rxSavePage(id, page.getString("markdown"))
                .doOnSuccess(v -> { // <1>
                    JsonObject event = new JsonObject()
                            .put("id", id) // <2>
                            .put("client", page.getString("client")); // <3>
                    //发布消息，页面保存
                    vertx.eventBus().publish("page.saved", event); // <4>
                })
                .subscribe(v -> apiResponse(context, 200, null, null), t -> apiFailure(context, t));
        // end::publish-on-page-updated[]
    }

    private boolean validateJsonPageDocument(RoutingContext context, JsonObject page, String... expectedKeys) {
        if (!Arrays.stream(expectedKeys).allMatch(page::containsKey)) {
            LOGGER.error("Bad page creation JSON payload: " + page.encodePrettily() + " from " + context.request().remoteAddress());
            context.response().setStatusCode(400);
            context.response().putHeader("Content-Type", "application/json");
            context.response().end(new JsonObject()
                    .put("success", false)
                    .put("error", "Bad request payload").encode());
            return false;
        }
        return true;
    }

    private void apiCreatePage(RoutingContext context) {
        JsonObject page = context.getBodyAsJson();
        if (!validateJsonPageDocument(context, page, "name", "markdown")) {
            return;
        }
        dbService.rxCreatePage(page.getString("name"), page.getString("markdown")).subscribe(
                v -> apiResponse(context, 201, null, null),
                t -> apiFailure(context, t));
    }

    private void apiGetPage(RoutingContext context) {
        int id = Integer.valueOf(context.request().getParam("id"));
        dbService.rxFetchPageById(id)
                .subscribe(dbObject -> {
                    if (dbObject.getBoolean("found")) {
                        JsonObject payload = new JsonObject()
                                .put("name", dbObject.getString("name"))
                                .put("id", dbObject.getInteger("id"))
                                .put("markdown", dbObject.getString("content"))
                                .put("html", Processor.process(dbObject.getString("content")));
                        apiResponse(context, 200, "page", payload);
                    } else {
                        apiFailure(context, 404, "There is no page with ID " + id);
                    }
                }, t -> apiFailure(context, t));
    }

    private void sayHiHandler(RoutingContext context) {
        String name = context.request().getParam("name");
        dbService.sayHi(name, reply -> {
            if (reply.succeeded()) {
                JsonObject payLoad = reply.result();
                String msg = payLoad.getString("msg", "无消息");
                context.response().putHeader("Content-Type", "text/html;charset=utf8");
                context.response().end(msg);

                //发布所有消息
                JsonObject page = new JsonObject();
                page.put("content",msg);
                //发布消息，页面保存
                vertx.eventBus().publish("page.sth_happen", page); // <4>
            }
        });
    }

    private void apiRoot(RoutingContext context) {
        dbService.rxFetchAllPagesData()
                .flatMapObservable(Observable::from)
                .map(obj -> new JsonObject()
                        .put("id", obj.getInteger("ID"))
                        .put("name", obj.getString("NAME")))
                .collect(JsonArray::new, JsonArray::add)
                .subscribe(pages -> apiResponse(context, 200, "pages", pages), t -> apiFailure(context, t));
    }

    private void apiResponse(RoutingContext context, int statusCode, String jsonField, Object jsonData) {
        context.response().setStatusCode(statusCode);
        context.response().putHeader("Content-Type", "application/json");
        JsonObject wrapped = new JsonObject().put("success", true);
        if (jsonField != null && jsonData != null) {
            wrapped.put(jsonField, jsonData);
        }
        context.response().end(wrapped.encode());
    }

    private void apiFailure(RoutingContext context, Throwable t) {
        apiFailure(context, 500, t.getMessage());
    }

    private void apiFailure(RoutingContext context, int statusCode, String error) {
        context.response().setStatusCode(statusCode);
        context.response().putHeader("Content-Type", "application/json");
        context.response().end(new JsonObject()
                .put("success", false)
                .put("error", error).encode());
    }
}
