package com.github.lkq.demo.googlespeech.rest;

import com.github.lkq.demo.googlespeech.recognition.SpeechAPIException;
import com.github.lkq.demo.googlespeech.recognition.SyncRecognizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

public class RoutesHandler {

    private static Logger logger = LoggerFactory.getLogger(RoutesHandler.class);

    private SyncRecognizer syncRecognizer;

    public RoutesHandler(SyncRecognizer syncRecognizer) {
        this.syncRecognizer = syncRecognizer;
    }

    public String handleAudioBuffer(Request request, Response response) {

        String sessionID = request.params("sessionID");
        Integer sequence = Integer.valueOf(request.queryParams("sequence"));

        byte[] bytes = request.bodyAsBytes();
        syncRecognizer.putBuffer(sessionID, sequence, bytes);
        logger.info("received {} bytes audio buffer with sessionID={}, sequence={}", bytes.length, sessionID, sequence);
        return "";
    }

    public String triggerRecognize(Request request, Response response) {

        String sessionID = request.params("sessionID");
        Integer sequence = Integer.valueOf(request.queryParams("sequence"));
        Integer sampleRate = Integer.valueOf(request.queryParams("sampleRate"));
        logger.info("triggering recognition with sessionID={}, sequence={}", sessionID, sequence);

        try {
            return syncRecognizer.recognize(sessionID, sequence, sampleRate);
        } catch (SpeechAPIException e) {
            response.status(e.getStatus());
            return e.getMessage();
        }
    }

}
