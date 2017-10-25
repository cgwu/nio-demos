《Building Reactive Microservices In Java》

创建项目命令:
mvn io.fabric8:vertx-maven-plugin:1.0.5:setup \
-DprojectGroupId=io.vertx.sample \
-DprojectArtifactId=my-first-vertx-app \
-Dverticle=io.vertx.sample.MyFirstVerticle


启动命令：
mvn compile vertx:run

Verticle有改动时会自动重新发布.

