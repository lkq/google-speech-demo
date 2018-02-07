package com.github.lkq.demo.googlespeech.config;

import spark.utils.StringUtils;

public class ConfigProvider {
    public String getAPIKey() {

        String apiKey = getParameter("GOOGLE_SPEECH_API_KEY");
        if (StringUtils.isBlank(apiKey)) {
            throw new RuntimeException("please provide speech API key by environment variable GOOGLE_SPEECH_API_KEY=<key> or by jvm parameter -DGOOGLE_SPEECH_API_KEY=<key>");
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
        return 1025;
    }

    public String getKeyStoreFile() {
        String keyStoreFile = getParameter("javax.net.ssl.keyStore");
        if (StringUtils.isBlank(keyStoreFile)) {
            throw new RuntimeException("please provide keystore file by environment variable javax.net.ssl.keyStore=<path> or by jvm parameter -Djavax.net.ssl.keyStore=<path>");
        }
        return keyStoreFile;
    }

    public String getKeyStorePwd() {
        return getParameter("javax.net.ssl.keyStorePassword");
    }

    private String getParameter(String key) {
        String value = System.getProperty(key);
        if (StringUtils.isBlank(value)) {
            value = System.getenv(key);
        }
        return value;
    }
}
