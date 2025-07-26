package com.example.owlpost_2_0.Gemini;

import okhttp3.*;
import java.io.IOException;

public class GeminiApiClient {
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/models/gemini-pro:generateContent";
    private final String apiKey;
    private final OkHttpClient client;

    public GeminiApiClient(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
    }

    public String generateContent(String prompt) throws IOException {
        // Create the request body
        String json = String.format("""
            {
                "contents": [{
                    "parts": [{
                        "text": "%s"
                    }]
                }]
            }
            """, prompt.replace("\"", "\\\""));

        RequestBody body = RequestBody.create(
                json,
                MediaType.parse("application/json; charset=utf-8")
        );

        // Build the request
        Request request = new Request.Builder()
                .url(BASE_URL + "?key=" + apiKey)
                .post(body)
                .build();

        // Execute the request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }
}