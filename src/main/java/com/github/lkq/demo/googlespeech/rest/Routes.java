package com.github.lkq.demo.googlespeech.rest;

import com.github.lkq.demo.googlespeech.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.put;
import static spark.Spark.staticFiles;

public class Routes {
    static private Logger logger = LoggerFactory.getLogger(Routes.class);

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
        put("/recognize", handler::handleSynchRecognize);
    }
}
