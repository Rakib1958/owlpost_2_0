package com.example.owlpost_2_0.VoiceAPI;

import javax.sound.sampled.*;
import java.io.File;
import java.util.Scanner;

public class recordAudio {

    public static void main(String[] args) throws Exception {
        AudioFormat format = new AudioFormat(44100.0f, 16, 1, true, false);
        TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
        microphone.open(format);
        microphone.start();

        File recording = new File("recording.wav");
        AudioInputStream audioInputStream = new AudioInputStream(microphone);

        System.out.println("Type start to begin...");

        Scanner input = new Scanner(System.in);
        String start = input.nextLine();

        if (start.equalsIgnoreCase("start")) {
            Thread stopper = new Thread(()->{
                try {
                    AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, recording);
                }catch (Exception e) {
                    System.out.println("Can't record audio : " + e);
                }
            });
            stopper.start();
            while (true) {
                if (input.nextLine().equalsIgnoreCase("stop")) {
                    break;
                }
            }
        }
        microphone.stop();
        microphone.close();
        System.out.println("Recording stopped..." + recording.getAbsolutePath());
    }
}
