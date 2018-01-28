package com.github.lkq.demo.googlespeech;

import com.github.lkq.demo.googlespeech.config.Config;
import com.github.lkq.demo.googlespeech.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {
    private static Logger logger = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        try {
            Config.init(new ConfigProvider());
            new Server().start();
        } catch (Throwable throwable) {
            logger.error("failed to start", throwable);
        }
    }
}
