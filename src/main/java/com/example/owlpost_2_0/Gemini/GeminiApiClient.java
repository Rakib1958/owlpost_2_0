package com.example.owlpost_2_0.Gemini;

import okhttp3.*;
import java.io.IOException;

public class GeminiApiClient {
    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private final String apiKey;
    private final OkHttpClient client;
    private long lastRequestTime = 0;
    private static final long MIN_REQUEST_INTERVAL = 2000;

    public GeminiApiClient(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }

    public String generateContent(String prompt) throws IOException, InterruptedException {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastRequestTime;
        if (timeSinceLastRequest < MIN_REQUEST_INTERVAL) {
            Thread.sleep(MIN_REQUEST_INTERVAL - timeSinceLastRequest);
        }
        lastRequestTime = System.currentTimeMillis();

        // Create the request body
        String json = String.format("""
            {
                "contents": [{
                    "parts": [{
                        "text": "%s"
                    }]
                }],
                "generationConfig": {
                    "temperature": 0.7,
                    "maxOutputTokens": 1000
                }
            }
            """, prompt.replace("\"", "\\\"").replace("\n", "\\n"));

        RequestBody body = RequestBody.create(
                json,
                MediaType.parse("application/json; charset=utf-8")
        );
        Request request = new Request.Builder()
                .url(BASE_URL + "?key=" + apiKey)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return response.body().string();
                } else if (response.code() == 429) {
                    if (attempt < maxRetries) {
                        System.out.println("Rate limited. Waiting before retry " + attempt + "...");
                        Thread.sleep(5000 * attempt);
                        continue;
                    }
                    throw new IOException("Rate limit exceeded after " + maxRetries + " attempts. Response: " + response);
                } else {
                    throw new IOException("HTTP error " + response.code() + ": " + response.message());
                }
            }
        }
        throw new IOException("Failed after " + maxRetries + " attempts");
    }
}