package com.github.lkq.demo.googlespeech;

import com.github.lkq.demo.googlespeech.config.Config;
import com.github.lkq.demo.googlespeech.config.LocalConfigProvider;

public class LocalLauncher {
    public static void main(String[] args) throws Exception {
        Config.init(new LocalConfigProvider());
        new Server().start();
    }
}
