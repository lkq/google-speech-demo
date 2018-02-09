package com.github.lkq.demo.googlespeech.recognition;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * extract transcript from google speech api response
 * sample response @resources/response.txt
 */
public class TranscriptExtractor {

    public String extractFromJson(String content) {
        JsonObject response = (JsonObject) new JsonParser().parse(content);
        JsonArray results = response.getAsJsonArray("results");
        if (results == null || results.size() <= 0) {
            return "(no results)";
        }
        JsonArray alternatives = results.get(0).getAsJsonObject().getAsJsonArray("alternatives");
        if (alternatives == null || alternatives.size() <= 0) {
            return "(no results)";
        }

        return alternatives.get(0).getAsJsonObject().get("transcript").getAsString();
    }
}
