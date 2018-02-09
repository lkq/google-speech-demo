package com.github.lkq.demo.googlespeech.recognition;

import com.google.gson.JsonObject;

import java.util.Base64;

public class RequestFactory {

    public String createRequest(byte[] audioBuffer, int sampleRate) {

        JsonObject config = createConfig(sampleRate);

        JsonObject audioObj = new JsonObject();
        audioObj.addProperty("content", Base64.getEncoder().encodeToString(audioBuffer));

        JsonObject requestObj = new JsonObject();
        requestObj.add("config", config);
        requestObj.add("audio", audioObj);

        return requestObj.toString();
    }

    public JsonObject createConfig(Integer sampleRate) {

        JsonObject config = new JsonObject();
        config.addProperty("encoding", "LINEAR16");
        config.addProperty("sampleRateHertz", sampleRate.toString());
        config.addProperty("languageCode", "en-US");
        config.addProperty("maxAlternatives", 1);
        return config;
    }
}
