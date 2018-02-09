package com.github.lkq.demo.googlespeech.rest;

import com.github.lkq.demo.googlespeech.config.Config;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.util.HashMap;
import java.util.Map;

public class SyncRecognizer {

    private static Logger logger = LoggerFactory.getLogger(SyncRecognizer.class);

    public static String url = "https://speech.googleapis.com/v1/speech:recognize";
    private final RequestFactory requestFactory;
    private Map<String, BufferAggregator> bufferAggregators;
    private TranscriptExtractor transcriptExtractor;

    private HttpSender httpSender;

    public SyncRecognizer(HttpSender httpSender, RequestFactory requestFactory) {
        this.bufferAggregators = new HashMap<>();
        this.httpSender = httpSender;
        this.requestFactory = requestFactory;
        this.transcriptExtractor = new TranscriptExtractor();
    }

    public void putBuffer(String sessionID, Integer sequence, byte[] bytes) {

        if (!bufferAggregators.containsKey(sessionID)) {
            bufferAggregators.put(sessionID, new BufferAggregator());
        }
        BufferAggregator aggregator = bufferAggregators.get(sessionID);
        aggregator.put(sequence, bytes);

    }

    public String recognize(String sessionID, Integer sequence, Integer sampleRate) throws SpeechAPIException {

        if (!bufferAggregators.containsKey(sessionID)) {
            bufferAggregators.put(sessionID, new BufferAggregator());
        }
        BufferAggregator aggregator = bufferAggregators.get(sessionID);
        aggregator.setFinalSequence(sequence);
        aggregator.setSampleRate(sampleRate);

        waitForReady(aggregator, (long) 20000);

        byte[] audioBuffer = aggregator.getBuffer();

        // playback the recorded sound if running in local
        if (Config.shouldPlayback()) {
            new Thread(() -> playback(audioBuffer, aggregator.getSampleRate())).run();
        }

        String request = requestFactory.createRequest(audioBuffer, sampleRate);
        ContentResponse response;
        try {
            response = httpSender.send(url, request);
        } catch (Throwable throwable) {
            logger.error("failed to send recognition request", throwable);
            throw new SpeechAPIException(500, throwable.getMessage());
        }
        if (response.getStatus() == HttpStatus.OK_200) {
            String content = response.getContentAsString();
            logger.info("recognize response: {}", content);
            String transcript = transcriptExtractor.extractFromJson(content);
            bufferAggregators.remove(sessionID);
            return transcript;
        } else {
            throw new SpeechAPIException(response.getStatus(), response.getContentAsString());
        }
    }

    private void waitForReady(BufferAggregator aggregator, long timeout) {
        while (!aggregator.isReady() && timeout > 0) {
            try {
                timeout -= 1000;
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void playback(byte[] buffer, Integer sampleRate) {
        try {
            logger.info("playing sound, size={}", buffer.length);

            Clip clip = AudioSystem.getClip();
            AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, 1, 2, sampleRate, false);
            clip.open(audioFormat, buffer, 0, buffer.length);
            clip.start();
        } catch (Throwable throwable) {
            logger.error("failed to playback audio", throwable);
        }
    }
}
