package com.github.lkq.demo.googlespeech.rest;

import com.google.gson.JsonObject;
import org.eclipse.jetty.client.api.ContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class SyncRecognizer {

    private static Logger logger = LoggerFactory.getLogger(SyncRecognizer.class);

    public static String url = "https://speech.googleapis.com/v1/speech:recognize";

    private SpeechSender speechSender;

    public SyncRecognizer(SpeechSender speechSender) {
        this.speechSender = speechSender;
    }

    public ContentResponse recognize(byte[] data) {
        try {

            JsonObject requestObj = new JsonObject();
            JsonObject config = new JsonObject();
            config.addProperty("encoding", "LINEAR16");
            config.addProperty("sampleRateHertz", "16000");
            config.addProperty("languageCode", "en-US");
            config.addProperty("maxAlternatives", 1);

            JsonObject audio = new JsonObject();
            audio.addProperty("content", Base64.getEncoder().encodeToString(data));

            requestObj.add("config", config);
            requestObj.add("audio", audio);

            String content = requestObj.toString();
            logger.info("sending synchronized speech recognition: " + content);

            return speechSender.send(url, content);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException("failed to send request", e);
        }
    }
}
