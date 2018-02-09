package com.github.lkq.demo.googlespeech.rest;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BufferAggregatorTest {

    private BufferAggregator aggregator;

    @Before
    public void setUp() throws Exception {
        aggregator = new BufferAggregator();
    }

    @Test
    public void canAggregateBuffer() throws Exception {
        aggregator.put(1, new byte[]{1});
        aggregator.put(0, new byte[]{2});
        byte[] buffer = aggregator.getBuffer();

        assertThat(buffer.length, is(2));
        assertThat(buffer[0], is((byte)2));
        assertThat(buffer[1], is((byte)1));
    }

    @Test
    public void testIsReadyWithinLimit() throws Exception {
        aggregator.put(1, new byte[]{1});
        aggregator.put(0, new byte[]{2});

        assertFalse(aggregator.isReady());

        aggregator.setFinalSequence(2);

        assertTrue(aggregator.isReady());
    }

    @Test
    public void testIsReadyExceedLimit() throws Exception {
        for (int i = 0; i < BufferAggregator.MAX_COUNT - 1; i++) {
            aggregator.put(i, new byte[]{1});
        }

        assertFalse(aggregator.isReady());

        aggregator.put(BufferAggregator.MAX_COUNT, new byte[]{1});

        assertTrue(aggregator.isReady());
    }
}