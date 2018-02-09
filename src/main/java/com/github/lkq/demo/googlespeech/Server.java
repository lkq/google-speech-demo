package com.github.lkq.demo.googlespeech;

import com.github.lkq.demo.googlespeech.config.Config;
import com.github.lkq.demo.googlespeech.rest.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import spark.Spark;

import java.util.logging.LogManager;

public class Server {

    private static Logger logger = LoggerFactory.getLogger(Server.class);

    public void start() throws Exception {
        setupLogging();

        HttpSender httpSender = new HttpSender(Config.getAPIKey());
        RequestFactory requestFactory = new RequestFactory();
        SyncRecognizer syncRecognizer = new SyncRecognizer(httpSender, requestFactory);
        RoutesHandler routesHandler = new RoutesHandler(syncRecognizer);

        Routes routes = new Routes(routesHandler);

        String keystoreFile = Config.getKeyStoreFile();
        String keystorePassword = Config.getKeyStorePwd();
        logger.info("enabling ssl, keystore={}", keystoreFile);
        Spark.secure(keystoreFile, keystorePassword, null, null);
        Spark.port(Config.getHttpPort());
        routes.start();
    }

    private void setupLogging() {
        // redirect jul to slf4j
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }
}
