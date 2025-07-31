package com.example.owlpost_2_0.VoiceAPI;

import com.assemblyai.api.RealtimeTranscriber;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;

import static com.example.owlpost_2_0.VoiceAPI.TestVoiceAPI.api;
import static com.example.owlpost_2_0.VoiceAPI.TestVoiceAPI.getKey;
import static java.lang.Thread.interrupted;

public class realtime {
    public static void main(String[] args) throws Exception{
        Thread thread = new Thread(()->{
            try {
                getKey();
                RealtimeTranscriber realtimeTranscriber = RealtimeTranscriber.builder()
                        .apiKey(api)
                        .sampleRate(16_000)
                        .onSessionStart(sessionBegins -> System.out.println("Session open ID: " + sessionBegins.getSessionId()))
                        .onPartialTranscript(transcript->{
                            if (!transcript.getText().isEmpty()) {
                                System.out.println("Partial : " + transcript.getText());
                            }
                        })
                        .onFinalTranscript(transcript-> System.out.println("Final : " + transcript.getText()))
                        .onError(err-> System.out.println("Error : " + err.getMessage()))
                        .build();
                System.out.println("Connecting to AssemblyAI...");
                realtimeTranscriber.connect();
                System.out.println("Start recording");
                AudioFormat format = new AudioFormat(16_000, 16,1, true, false);
                TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
                microphone.open(format);
                byte[] data = new byte[microphone.getBufferSize()];
                microphone.start();
                while (!interrupted()) {
                    microphone.read(data, 0, data.length);
                    realtimeTranscriber.sendAudio(data);
                }
                System.out.println("Stopping recording");
                microphone.close();
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();

        System.out.println("Enter to stop...");
        System.in.read();
        thread.interrupt();
        System.exit(0);
    }
}