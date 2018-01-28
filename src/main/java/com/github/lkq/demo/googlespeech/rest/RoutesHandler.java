package com.github.lkq.demo.googlespeech.rest;

import org.eclipse.jetty.client.api.ContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

public class RoutesHandler {

    private static Logger logger = LoggerFactory.getLogger(RoutesHandler.class);

    SyncRecognizer syncRecognizer;

    public RoutesHandler(SyncRecognizer syncRecognizer) {
        this.syncRecognizer = syncRecognizer;
    }

    public String handleSynchRecognize(Request request, Response response) {

        byte[] data = request.bodyAsBytes();
        logger.info("received {} bytes", data.length);
        ContentResponse recResponse = syncRecognizer.recognize(data);

        response.status(recResponse.getStatus());
        return recResponse.getContentAsString();
    }
}
