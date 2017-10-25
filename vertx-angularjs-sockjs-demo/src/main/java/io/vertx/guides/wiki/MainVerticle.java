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

package io.vertx.guides.wiki;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.rxjava.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Single;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
public class MainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        Single<String> dbVerticleDeployment = vertx.rxDeployVerticle("io.vertx.guides.wiki.database.WikiDatabaseVerticle");

        dbVerticleDeployment.flatMap(id -> {
            LOGGER.info("WikiDatabaseVerticle ID:{}", id);
            return vertx.rxDeployVerticle("io.vertx.guides.wiki.http.HttpServerVerticle",
                    new DeploymentOptions().setInstances(2));
        }).subscribe(
                id -> {
                    LOGGER.info("subscribe print ID:{}", id);
                    startFuture.complete();
                },
                startFuture::fail);
    }
}
