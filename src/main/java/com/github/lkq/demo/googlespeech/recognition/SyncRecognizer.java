package com.github.lkq.demo.googlespeech.recognition;

import com.github.lkq.demo.googlespeech.config.Config;
import com.github.lkq.demo.googlespeech.rest.HttpSender;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SyncRecognizer {

    private static Logger logger = LoggerFactory.getLogger(SyncRecognizer.class);

    public static String url = "https://speech.googleapis.com/v1/speech:recognize";

    /**
     * max session life span, a session should be house keep after timeout
     */
    public static long SESSION_TIMEOUT = 60000;

    private final RequestFactory requestFactory;
    private Map<String, BufferAggregator> bufferAggregators;
    private TranscriptExtractor transcriptExtractor;
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private HttpSender httpSender;

    public SyncRecognizer(HttpSender httpSender, RequestFactory requestFactory) {
        this.bufferAggregators = Collections.synchronizedMap(new HashMap<>());
        this.httpSender = httpSender;
        this.requestFactory = requestFactory;
        this.transcriptExtractor = new TranscriptExtractor();
        executor.scheduleAtFixedRate(this::houseKeep, SESSION_TIMEOUT, SESSION_TIMEOUT, TimeUnit.MILLISECONDS);
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
            new Thread(() -> AudioPlayer.play(audioBuffer, aggregator.getSampleRate())).run();
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

    /**
     * wait for all audio packages arrive.
     * @param aggregator
     * @param timeout
     */
    private void waitForReady(BufferAggregator aggregator, long timeout) {
        while (!aggregator.isReady() && timeout > 0) {
            try {
                timeout -= 1000;
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
    }


    /**
     * house keep timeout sessions
     */
    private void houseKeep() {
        List<String> houseKeepTarget = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, BufferAggregator> entry : bufferAggregators.entrySet()) {
            if (currentTime - entry.getValue().getCreationTime() > SESSION_TIMEOUT) {
                logger.info("flag timeout session, sessionID={}, creation time={}", entry.getKey(), entry.getValue().getCreationTime());
                houseKeepTarget.add(entry.getKey());
            }
        }
        for (String key : houseKeepTarget) {
            bufferAggregators.remove(key);
        }
    }
}
