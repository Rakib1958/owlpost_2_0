package com.example.owlpost_2_0.VoiceAPI;

import com.assemblyai.api.AssemblyAI;
import com.assemblyai.api.resources.transcripts.types.Transcript;

import java.io.BufferedReader;
import java.io.FileReader;

public class TestVoiceAPI {
    public static void main(String[] args) {
        String api = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader("assemblyaiAPI.txt"));
            api = reader.readLine();
            reader.close();
        }catch (Exception e) {
            System.out.println("Can't read key : " + e);
        }
        AssemblyAI assemblyAI = AssemblyAI.builder().apiKey(api).build();

        Transcript transcript = assemblyAI.transcripts().transcribe("recording.wav");
        System.out.println(transcript.getText());
    }
}
