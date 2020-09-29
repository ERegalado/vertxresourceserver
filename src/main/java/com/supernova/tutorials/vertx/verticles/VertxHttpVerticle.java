package com.supernova.tutorials.vertx.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;


public class VertxHttpVerticle extends AbstractVerticle {
    @Override
    public void start() {
        Router router = configureRouter();
        vertx.createHttpServer().requestHandler(router).listen(8080);
    }

    private Router configureRouter() {
        Router router = Router.router(vertx);

        router.route("/").handler(routingContext -> {
            routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/html")
                    .end("<h1>The Vertx server is running!</h1>");
        });

        router.route("/api/*").handler(this::validateTokens);

        router.route("/api/users").handler(routingContext -> {
            routingContext.response().setStatusCode(200);
            routingContext.response().end("Here goes a secret list of users");
        });

        router.route("/api/secret").handler(routingContext -> {
            routingContext.response().setStatusCode(200);
            routingContext.response().end("This is a super SECRET endpoint");
        });

        router.route("/api/admin").handler(routingContext -> {
            routingContext.response().setStatusCode(200);
            routingContext.response().end("Only users with a valid token have access to this endpoint");
        });

        return router;
    }

    private void validateTokens(RoutingContext routingContext) {
        JWTAuth provider = JWTAuth.create(vertx, new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions(new JsonObject().put("algorithm", "RS256").put("publicKey",
                        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoKIgj4Q7ouWtjrVUcSCLjscOPe/Yb8gVy44kIc6r3CpoMp9wXHngfRAsrWOSINbZfIQp9PY0scf+EukYKiXkO2R9+f3nQmqo+j8last8px9u+9rEA0s8SNVwLcB2kdrv6fj4uzKf5MsUNZOVHaHYEhhafgvuTGEaAozh9fcrI1gyUTExmdUzLVmE8H2zQ6LNt+EYKhSXvTI7YY3P1Yuc+askS1cH7oF3b4Pd5PK2/rFolQIC/5Y0hA/Ub+feX+pA62l7/pnWGGG4sOP/R/Hl/E+nA5xGgQ5axWdA7AtPyWQSuUDz97WbisskykycKAMT8Exxeb6ZCt4Gfb+tVZvN0QIDAQAB"))));
        String jwt = routingContext.request().getHeader("Authorization");
        if (jwt != null && !jwt.equals("")) {
            jwt = jwt.substring(7);
            provider.authenticate(new JsonObject().put("jwt", jwt),
                    result -> {
                        if (result.succeeded()) {
                            routingContext.next();
                        } else {
                            routingContext.response().setStatusCode(401);
                            routingContext.response().end();
                        }
                    });
        } else {
            routingContext.response().setStatusCode(401);
            routingContext.response().end();
        }
    }
}
