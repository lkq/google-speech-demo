package com.github.lkq.demo.googlespeech.config;

import spark.utils.StringUtils;

public class ConfigProvider {
    public String getAPIKey() {

        String apiKey = System.getenv("GOOGLE_SPEECH_API_KEY");
        if (StringUtils.isBlank(apiKey)) {
            throw new RuntimeException("please provide speech API key with environment variable GOOGLE_SPEECH_API_KEY");
        }
        return apiKey;
    }

    public String getWebRoot() {
        return "webroot";
    }

    public boolean useExternalStatic() {
        return false;
    }

    public int getHttpPort() {
        return 3000;
    }
}
