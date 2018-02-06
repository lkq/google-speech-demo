package com.github.lkq.demo.googlespeech.config;

public class LocalConfigProvider extends ConfigProvider {
    @Override
    public String getWebRoot() {
        return "src/main/resources/webroot";
    }

    @Override
    public boolean useExternalStatic() {
        return true;
    }

    @Override
    public String getKeyStoreFile() {
        return "src/test/resources/google-speech-demo.jks";
    }

    @Override
    public String getKeyStorePwd() {
        return "abcd1234";
    }
}
