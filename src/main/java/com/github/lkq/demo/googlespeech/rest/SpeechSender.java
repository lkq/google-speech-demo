package com.github.lkq.demo.googlespeech.rest;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class SpeechSender {

    private final HttpClient client;
    private String apiKey;

    public SpeechSender(String apiKey) throws Exception {
        this.apiKey = apiKey;
        client = new HttpClient(new SslContextFactory(true));
        client.start();
    }

    public ContentResponse send(String url, String content) throws InterruptedException, ExecutionException, TimeoutException {
        return client.POST(url)
                .content(new StringContentProvider(content))
                .param("key", apiKey)
                .send();
    }
}
