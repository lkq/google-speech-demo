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
     * limit the max co-exist sessions to avoid excessive resource consumption
     */
    public static long MAX_SESSION_COUNT = 10;

    /**
     * max session life span, a session should be house kept after timeout
     */
    public static long SESSION_TIMEOUT = 60000;

    /**
     * each request is rounded to 15 seconds for pricing, set the usage limit to 600 seconds, that's 40 requests at most per restart
     */
    public static long USAGE_LIMIT = 600;

    private final RequestFactory requestFactory;
    private Map<String, BufferAggregator> bufferAggregators;
    private TranscriptExtractor transcriptExtractor;
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private HttpSender httpSender;
    private long accumulatedTime = 0;

    public SyncRecognizer(HttpSender httpSender, RequestFactory requestFactory, TranscriptExtractor transcriptExtractor) {
        this.bufferAggregators = Collections.synchronizedMap(new HashMap<>());
        this.httpSender = httpSender;
        this.requestFactory = requestFactory;
        this.transcriptExtractor = transcriptExtractor;
        executor.scheduleAtFixedRate(this::houseKeep, SESSION_TIMEOUT, SESSION_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public void putBuffer(String sessionID, Integer sequence, byte[] bytes) {

        BufferAggregator aggregator = getAggregator(sessionID);
        aggregator.put(sequence, bytes);

    }

    public String recognize(String sessionID, Integer sequence, Integer sampleRate) throws SpeechAPIException {

        BufferAggregator aggregator = getAggregator(sessionID);
        aggregator.setFinalSequence(sequence);
        aggregator.setSampleRate(sampleRate);

        waitForReady(aggregator, (long) 20000);

        byte[] audioBuffer = aggregator.getBuffer();

        if (exceedUsageLimit(audioBuffer, sampleRate)) {
            return "Sorry, exceeded usage limit, the limit will be reset after restart";
        }

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

    private BufferAggregator getAggregator(String sessionID) {
        if (!bufferAggregators.containsKey(sessionID)) {
            if (bufferAggregators.size() > MAX_SESSION_COUNT) {
                throw new RuntimeException("too many sessions");
            }
            bufferAggregators.put(sessionID, new BufferAggregator());
        }
        return bufferAggregators.get(sessionID);
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

    /**
     * set a usage limit to avoid unexpected charge
     * @param audioBuffer
     * @param sampleRate
     * @return
     */
    private boolean exceedUsageLimit(byte[] audioBuffer, Integer sampleRate) {
        accumulatedTime += audioBuffer.length / sampleRate;
        logger.info("accumulated usage: {}", accumulatedTime);
        if (accumulatedTime > USAGE_LIMIT) {
            return true;
        } else {
            return false;
        }
    }
}
