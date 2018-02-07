package com.github.lkq.demo.googlespeech.rest;

import com.google.gson.JsonObject;

public class ConfigFactory {
    public JsonObject create() {

        JsonObject config = new JsonObject();
        config.addProperty("encoding", "LINEAR16");
        config.addProperty("sampleRateHertz", "16000");
        config.addProperty("languageCode", "en-US");
        config.addProperty("maxAlternatives", 1);
        return config;
    }
}
