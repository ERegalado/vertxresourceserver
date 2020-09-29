package com.supernova.tutorials.vertx;

import com.supernova.tutorials.vertx.verticles.VertxHttpVerticle;
import io.vertx.core.Vertx;

public class VertxHttpAPI {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new VertxHttpVerticle(), res -> {
            if (res.succeeded()) {
                System.out.println("HTTP server listening on port 8080");
            }
        });
    }
}