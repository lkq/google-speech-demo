package com.github.lkq.demo.googlespeech.rest;

import com.github.lkq.demo.googlespeech.config.Config;
import org.eclipse.jetty.client.api.ContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.util.HashMap;
import java.util.Map;

public class RoutesHandler {

    private static Logger logger = LoggerFactory.getLogger(RoutesHandler.class);

    private Map<String, BufferAggregator> bufferAggregators;

    private SyncRecognizer syncRecognizer;

    public RoutesHandler(SyncRecognizer syncRecognizer) {
        this.bufferAggregators = new HashMap<>();
        this.syncRecognizer = syncRecognizer;
    }

    public String handleSyncRecognize(Request request, Response response) {

        String sessionID = request.params("sessionID");
        Boolean finished = Boolean.valueOf(request.queryParams("finished"));
        Integer sequence = Integer.valueOf(request.queryParams("sequence"));

        if (!bufferAggregators.containsKey(sessionID)) {
            bufferAggregators.put(sessionID, new BufferAggregator());
        }
        if (!finished) {
            BufferAggregator aggregator = bufferAggregators.get(sessionID);
            byte[] bytes = request.bodyAsBytes();
            aggregator.put(sequence, bytes);
            logger.info("received {} bytes with sessionID={}, sequence={}", bytes.length, sessionID, sequence);

            return "OK";
        } else {
            logger.info("received final request with sessionID={}, sequence={}", sessionID, sequence);

            BufferAggregator aggregator = bufferAggregators.get(sessionID);
            aggregator.putFinal(sequence);
            Integer sampleRate = Integer.valueOf(request.queryParams("sampleRate"));
            aggregator.setSampleRate(sampleRate);
            long timeout = 30000;
            while (!aggregator.isReady() && timeout > 0) {
                try {
                    timeout -= 1000;
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
            return aggregateAndSend(response, sessionID);
        }

    }

    private String aggregateAndSend(Response response, String sessionID) {
        BufferAggregator aggregator = bufferAggregators.get(sessionID);
        bufferAggregators.remove(sessionID);

        byte[] allBytes = aggregator.getAllBytes();

        // playback the recorded sound if running in local
        if (Config.shouldPlayback()) {
            new Thread(() -> playback(allBytes, aggregator.getSampleRate())).run();
        }

        ContentResponse recResponse = syncRecognizer.recognize(allBytes, aggregator.getSampleRate());

        response.status(recResponse.getStatus());
        String responseContent = recResponse.getContentAsString();
        logger.info("recognize response: {}", responseContent);

        return responseContent;
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
