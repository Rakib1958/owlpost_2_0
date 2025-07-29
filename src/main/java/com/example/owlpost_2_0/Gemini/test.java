package com.example.owlpost_2_0.Gemini;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;

public class test {
    public static void main(String[] args) {
        String apiKey = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader("apikey.txt"));
            apiKey = reader.readLine();
            reader.close();

            if (apiKey == null || apiKey.trim().isEmpty()) {
                System.out.println("API key is empty or null");
                return;
            }

        } catch (Exception e) {
            System.out.println("Can't read key: " + e.getMessage());
            return;
        }

        GeminiApiClient apiClient = new GeminiApiClient(apiKey);
        Scanner input = new Scanner(System.in);

        System.out.println("Gemini Chat Client Started. Type 'sayonara' to exit.");

        while (true) {
            System.out.print("\nEnter something: ");
            String prompt = input.nextLine();

            if (prompt.toLowerCase().contains("sayonara")) {
                System.out.println("Goodbye!");
                break;
            }

            if (prompt.trim().isEmpty()) {
                System.out.println("Please enter a non-empty prompt.");
                continue;
            }

            try {
                System.out.println("Sending request... (please wait)");
                String response = apiClient.generateContent(prompt);
                JSONObject jsonResponse = new JSONObject(response);

                if (jsonResponse.has("error")) {
                    JSONObject error = jsonResponse.getJSONObject("error");
                    System.out.println("API Error: " + error.getString("message"));
                    continue;
                }

                JSONArray candidates = jsonResponse.getJSONArray("candidates");
                if (candidates.length() > 0) {
                    JSONObject candidate = candidates.getJSONObject(0);
                    JSONObject content = candidate.getJSONObject("content");
                    JSONArray parts = content.getJSONArray("parts");
                    if (parts.length() > 0) {
                        String text = parts.getJSONObject(0).getString("text");
                        System.out.println("\nResponse: " + text);
                    }
                } else {
                    System.out.println("No response generated.");
                }

            } catch (java.io.IOException e) {
                System.out.println("Network/API Error: " + e.getMessage());
                if (e.getMessage().contains("429")) {
                    System.out.println("You're being rate limited. Please wait a few minutes before trying again.");
                }
            } catch (InterruptedException e) {
                System.out.println("Request was interrupted: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        input.close();
    }
}