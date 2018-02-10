package com.github.lkq.demo.googlespeech.rest;

import com.github.lkq.demo.googlespeech.recognition.SpeechAPIException;
import com.github.lkq.demo.googlespeech.recognition.SyncRecognizer;
import org.eclipse.jetty.http.HttpStatus;
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

        String sessionID;
        Integer sequence;
        byte[] bytes;
        try {
            sessionID = request.params("sessionID");
            sequence = Integer.valueOf(request.queryParams("sequence"));

            bytes = request.bodyAsBytes();
        } catch (Throwable throwable) {
            response.status(HttpStatus.BAD_REQUEST_400);
            return throwable.getMessage();
        }
        try {
            syncRecognizer.putBuffer(sessionID, sequence, bytes);
            logger.info("received {} bytes audio buffer with sessionID={}, sequence={}", bytes.length, sessionID, sequence);
        } catch (Throwable throwable) {
            response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            return throwable.getMessage();
        }
        return "";
    }

    public String triggerRecognize(Request request, Response response) {

        String sessionID;
        Integer sequence;
        Integer sampleRate;
        try {
            sessionID = request.params("sessionID");
            sequence = Integer.valueOf(request.queryParams("sequence"));
            sampleRate = Integer.valueOf(request.queryParams("sampleRate"));
        } catch (Throwable throwable) {
            response.status(HttpStatus.BAD_REQUEST_400);
            return throwable.getMessage();
        }

        try {
            logger.info("triggering recognition with sessionID={}, sequence={}", sessionID, sequence);
            return syncRecognizer.recognize(sessionID, sequence, sampleRate);
        } catch (SpeechAPIException e) {
            response.status(e.getStatus());
            return e.getMessage();
        } catch (Throwable throwable) {
            response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            return throwable.getMessage();
        }
    }

}
