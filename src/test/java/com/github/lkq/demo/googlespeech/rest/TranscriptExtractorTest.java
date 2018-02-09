package com.github.lkq.demo.googlespeech.rest;

import com.github.lkq.demo.googlespeech.recognition.TranscriptExtractor;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TranscriptExtractorTest {
    @Test
    public void canExtractNormalResponse() throws Exception {
        TranscriptExtractor extractor = new TranscriptExtractor();
        String transcript = extractor.extractFromJson("{\n" +
                "  \"results\": [\n" +
                "    {\n" +
                "      \"alternatives\": [\n" +
                "        {\n" +
                "          \"transcript\": \"how are you\",\n" +
                "          \"confidence\": 0.9545434\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}\n");

        assertThat(transcript, is("how are you"));
    }
}