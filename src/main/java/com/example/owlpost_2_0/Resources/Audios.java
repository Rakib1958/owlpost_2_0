package com.example.owlpost_2_0.Resources;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class Audios {
    private static Media ambience = new Media(Audios.class.getResource("/com/example/owlpost_2_0/Music/Ambience.mp3").toExternalForm());
    private static Media BGM;
//    private static Media BGM = new Media(Audios.class.getResource("/com/example/owlpost_2_0/Music/BGM.mp3").toExternalForm());

    private static MediaPlayer ambiencePlayer = new MediaPlayer(ambience);
    private static MediaPlayer BGMPlayer;
//    private static MediaPlayer BGMPlayer = new MediaPlayer(BGM);

    public static void playAmbience() {
        ambiencePlayer.setVolume(0.25);
        ambiencePlayer.setCycleCount(MediaPlayer.INDEFINITE);
        ambiencePlayer.play();
    }

    public static void stopAmbience() {
        if (ambiencePlayer != null) {
            ambiencePlayer.stop();
        }
    }

    public static void playBGM(String currentMusic) {
        BGM = new Media(Audios.class.getResource("/com/example/owlpost_2_0/Music/" + currentMusic + ".mp3").toExternalForm());
        BGMPlayer = new MediaPlayer(BGM);
        BGMPlayer.setVolume(0.5);
        BGMPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        BGMPlayer.play();

    }

    public static void stopBGM() {
        if (BGMPlayer != null) {
            BGMPlayer.stop();
        }
    }

    public static void playSound(String sound) {
        Media media = new Media(Audios.class.getResource("/com/example/owlpost_2_0/Music/" + sound + ".mp3").toExternalForm());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        if (sound.equals("spell")) {
            mediaPlayer.setVolume(0.25);
        } else {
            mediaPlayer.setVolume(1);
        }
        mediaPlayer.play();
        mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.dispose());
    }

}