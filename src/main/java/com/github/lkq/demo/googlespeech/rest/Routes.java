package com.github.lkq.demo.googlespeech.rest;

import com.github.lkq.demo.googlespeech.config.Config;

import static spark.Spark.*;

public class Routes {

    private RoutesHandler handler;

    public Routes(RoutesHandler handler) {
        this.handler = handler;
    }

    public void start() {
        if (Config.useExternalStatic()) {
            staticFiles.externalLocation(Config.getWebRoot());
        } else {
            staticFiles.location(Config.getWebRoot());
        }
        put("/recognize", handler::handleSyncRecognize);
    }
}
