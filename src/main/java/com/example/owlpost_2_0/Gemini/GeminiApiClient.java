package com.example.owlpost_2_0.Gemini;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class GeminiApiClient {
    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private final String apiKey;
    private final OkHttpClient client;
    private long lastRequestTime = 0;
    private static final long MIN_REQUEST_INTERVAL = 2000;

    public GeminiApiClient(String apiKey) {
        this.apiKey = apiKey;
        System.out.println(apiKey);
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

        JSONObject part = new JSONObject().put("text", prompt);
        JSONArray parts = new JSONArray().put(part);
        JSONObject content = new JSONObject().put("parts", parts);
        JSONArray contents = new JSONArray().put(content);

        JSONObject generationConfig = new JSONObject();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 1000);

        JSONObject payload = new JSONObject();
        payload.put("contents", contents);
        payload.put("generationConfig", generationConfig);

        String json = payload.toString();
//        System.out.println("Request JSON:\n" + json);

        RequestBody body = RequestBody.create(
                json, MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                System.out.println("HTTP Code: " + response.code());
                System.out.println("Error Body: " + responseBody);
                throw new IOException("HTTP error " + response.code() + ": " + response.message());
            }
            return responseBody;
        }
    }

}