package com.supernova.tutorials.vertx.verticles;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.net.URL;
import java.util.Base64;


public class VertxHttpVerticle extends AbstractVerticle {

    private final String JWKS_URL = "http://localhost:8083/auth/realms/baeldung/protocol/openid-connect/certs";
    private final String JWKS_KID = "_b78X30O343js3QZcvCJSSHa4zUKPmIBchQmHcNpBUM";

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
                .addPubSecKey(new PubSecKeyOptions(new JsonObject().put("algorithm", "RS256").put("publicKey", getKey()))));
        String jwt = routingContext.request().getHeader("Authorization");
        if (jwt != null && !jwt.equals("")) {
            jwt = jwt.substring(7);
            provider.authenticate(new JsonObject().put("jwt", jwt),
                    result -> {
                        if (result.succeeded()) {
                            routingContext.next();
                        } else {
                            setUnauthorized(routingContext);
                        }
                    });
        } else {
            setUnauthorized(routingContext);
        }
    }

    private String getKey() {
        try {
            JwkProvider provider = new UrlJwkProvider(new URL(JWKS_URL));
            Jwk jwk = provider.get(JWKS_KID);
            return Base64.getEncoder().encodeToString(jwk.getPublicKey().getEncoded());
        } catch (Exception e) {
            return "";
        }
    }

    private void setUnauthorized(RoutingContext routingContext) {
        routingContext.response().setStatusCode(401);
        routingContext.response().end();
    }
}
