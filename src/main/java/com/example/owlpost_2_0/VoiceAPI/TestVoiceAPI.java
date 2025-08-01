package com.example.owlpost_2_0.VoiceAPI;

import com.assemblyai.api.AssemblyAI;
import com.assemblyai.api.resources.transcripts.types.Transcript;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.owlpost_2_0.VoiceAPI.recordAudio.record;

public class TestVoiceAPI {
    public static String api = "";
    public static void getKey() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("assemblyaiAPI.txt"));
            api = reader.readLine();
            reader.close();
        }catch (Exception e) {
            System.out.println("Can't read key : " + e);
        }
    }

    public static String uploadFile(String filePath, String apiKey) throws IOException {
        File audioFile = new File(filePath);
        URL url = new URL("https://api.assemblyai.com/v2/upload");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("authorization", apiKey);
        conn.setDoOutput(true);

        try (BufferedOutputStream out = new BufferedOutputStream(conn.getOutputStream());
             FileInputStream in = new FileInputStream(audioFile)) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                //{"upload_url":"https://cdn.assemblyai.com/upload/xyz.wav"}
                String json = response.toString();
                String audioUrl = json.split(":\"")[1].split("\"")[0];
                return audioUrl;
            }
        } else {
            throw new IOException("Failed to upload file. HTTP Code: " + responseCode);
        }
    }

    public static void main(String[] args) {


        try {
            getKey();
            record();
            AssemblyAI assemblyAI = AssemblyAI.builder().apiKey(api).build();
            String url = uploadFile("recording.wav", api);
            System.out.println(url);
            Transcript transcript = assemblyAI.transcripts().transcribe(url);
            System.out.println(transcript.getText());
        }catch (Exception e) {
            System.out.println("Can't send");
        }
    }
}
