package com.github.lkq.demo.googlespeech.rest;

import org.eclipse.jetty.client.api.ContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

public class RoutesHandler {

    private static Logger logger = LoggerFactory.getLogger(RoutesHandler.class);

    private SyncRecognizer syncRecognizer;
    private LongRunRecognizer longRunRecognizer;

    public RoutesHandler(SyncRecognizer syncRecognizer, LongRunRecognizer longRunRecognizer) {
        this.syncRecognizer = syncRecognizer;
        this.longRunRecognizer = longRunRecognizer;
    }

    public String handleSyncRecognize(Request request, Response response) {

        byte[] data = request.bodyAsBytes();
        logger.info("received {} bytes", data.length);
        ContentResponse recResponse = syncRecognizer.recognize(data);

        response.status(recResponse.getStatus());
        String responseContent = recResponse.getContentAsString();
        logger.info("recognize response: {}", responseContent);
        return responseContent;
    }

    public String handleAsyncRecognize(Request request, Response response) {

        byte[] data = request.bodyAsBytes();
        logger.info("received {} bytes", data.length);
        return "OK";
    }
}
