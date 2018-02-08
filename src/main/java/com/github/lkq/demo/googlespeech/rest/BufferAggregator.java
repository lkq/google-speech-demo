package com.github.lkq.demo.googlespeech.rest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class BufferAggregator {
    private Map<Integer, byte[]> buffer = new HashMap<>();
    private int bufferCount = -1;
    private Integer sampleRate;

    public void put(Integer seq, byte[] buf) {
        buffer.put(seq, buf);
    }

    public byte[] getAllBytes() {

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
        return bufferCount > 0 && buffer.size() >= bufferCount;
    }

    public void putFinal(Integer sequence) {
        bufferCount = sequence;
    }

    public void setSampleRate(Integer sampleRate) {
        this.sampleRate = sampleRate;
    }

    public Integer getSampleRate() {
        return sampleRate;
    }
}
