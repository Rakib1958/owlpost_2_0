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
        }catch (Exception e) {
            System.out.println("Cant read key : " + e);
        }
        GeminiApiClient apiClient = new GeminiApiClient(apiKey);
        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.println("Enter something...");
            String prompt = input.nextLine();
            if (prompt.toLowerCase().contains("sayonara")) {
                break;
            }
            try {
                String response = apiClient.generateContent(prompt);

                // Parse the JSON response
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray candidates = jsonResponse.getJSONArray("candidates");
                if (candidates.length() > 0) {
                    JSONObject candidate = candidates.getJSONObject(0);
                    JSONObject content = candidate.getJSONObject("content");
                    JSONArray parts = content.getJSONArray("parts");
                    if (parts.length() > 0) {
                        String text = parts.getJSONObject(0).getString("text");

                        // Update the UI on the JavaFX Application Thread
                        System.out.println(text);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error : ");
                e.printStackTrace();
            }
        }
    }
}
