package com.github.lkq.demo.googlespeech;

import com.github.lkq.demo.googlespeech.config.Config;
import com.github.lkq.demo.googlespeech.rest.Routes;
import com.github.lkq.demo.googlespeech.rest.RoutesHandler;
import com.github.lkq.demo.googlespeech.rest.SpeechSender;
import com.github.lkq.demo.googlespeech.rest.SyncRecognizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import spark.Spark;

import java.util.logging.LogManager;

public class Server {

    private static Logger logger = LoggerFactory.getLogger(Server.class);

    private SpeechSender speechSender;
    private SyncRecognizer syncRecognizer;
    private RoutesHandler routesHandler;
    private Routes routes;

    public void start() throws Exception {
        setupLogging();

        speechSender = new SpeechSender(Config.getAPIKey());
        syncRecognizer = new SyncRecognizer(speechSender);
        routesHandler = new RoutesHandler(syncRecognizer);

        routes = new Routes(routesHandler);

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
