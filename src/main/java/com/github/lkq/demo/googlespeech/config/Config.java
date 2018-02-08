package com.github.lkq.demo.googlespeech.config;

public class Config {
    private static ConfigProvider provider;

    public static void init(ConfigProvider provider) {
        Config.provider = provider;
    }

    public static String getAPIKey() {
        return provider.getAPIKey();
    }

    public static String getWebRoot() {
        return provider.getWebRoot();
    }

    public static boolean useExternalStatic() {
        return provider.useExternalStatic();
    }

    public static int getHttpPort() {
        return provider.getHttpPort();
    }

    public static String getKeyStoreFile() {
        return provider.getKeyStoreFile();
    }

    public static String getKeyStorePwd() {
        return provider.getKeyStorePwd();
    }

    public static boolean shouldPlayback() {
        return provider.shouldPlayback();
    }
}
