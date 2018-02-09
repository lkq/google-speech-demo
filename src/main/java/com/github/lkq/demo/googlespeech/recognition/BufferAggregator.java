package com.github.lkq.demo.googlespeech.recognition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * cache the audio package, and aggregate by sequences as a whole for recognition.
 */
public class BufferAggregator {
    private static Logger logger = LoggerFactory.getLogger(BufferAggregator.class);

    /**
     * due to 60 seconds limitation of google speech rest api, need to limit the max allowed audio packages,
     * if receives more then MAX_COUNT, the latest package will be disposed.
     * under a bad network, the earlier audio package may arrive after the later ones, and may got disposed
     * it does not worth the effort to handle that case for demo purpose
     */
    public static int MAX_COUNT = 100;

    private Map<Integer, byte[]> buffer = new HashMap<>();
    private int finalSequence = -1;
    private Integer sampleRate;
    private long creationTime;

    public BufferAggregator() {
        creationTime = System.currentTimeMillis();
    }

    public void put(Integer seq, byte[] buf) {
        if (buffer.size() < MAX_COUNT) {
            buffer.put(seq, buf);
        } else {
            logger.info("reach max buffer count, disposing new data, size=" + buf.length);
        }
    }

    public byte[] getBuffer() {

        ArrayList<Integer> sequences = new ArrayList<>(buffer.keySet());
        sequences.sort(Comparator.naturalOrder());
        int size = 0;
        for (Integer seq : sequences) {
            size += buffer.get(seq).length;
        }

        byte[] allBytes = new byte[size];
        int pos = 0;
        for (Integer seq : sequences) {
            byte[] bytes = buffer.get(seq);
            System.arraycopy(bytes, 0, allBytes, pos, bytes.length);
            pos += bytes.length;
        }
        return allBytes;
    }

    public boolean isReady() {
        if (finalSequence > 0) {
            return buffer.size() >= Math.min(finalSequence, MAX_COUNT);
        } else {
            return buffer.size() >= MAX_COUNT;
        }
    }

    public void setFinalSequence(Integer sequence) {
        finalSequence = sequence;
    }

    public void setSampleRate(Integer sampleRate) {
        this.sampleRate = sampleRate;
    }

    public Integer getSampleRate() {
        return sampleRate;
    }

    public long getCreationTime() {
        return creationTime;
    }
}
