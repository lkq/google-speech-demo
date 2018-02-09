package com.github.lkq.demo.googlespeech.recognition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class AudioPlayer {
    private static Logger logger = LoggerFactory.getLogger(AudioPlayer.class);

    /**
     * expected sample size to be 16 bits
     */
    public static final int SAMPLE_SIZE = 16;

    public static void play(byte[] buffer, Integer sampleRate) {
        try {
            logger.info("playing sound, size={}", buffer.length);

            Clip clip = AudioSystem.getClip();
            AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, SAMPLE_SIZE, 1, SAMPLE_SIZE / 8, sampleRate, false);
            clip.open(audioFormat, buffer, 0, buffer.length);
            clip.start();
        } catch (Throwable throwable) {
            logger.error("failed to playback audio", throwable);
        }
    }
}
