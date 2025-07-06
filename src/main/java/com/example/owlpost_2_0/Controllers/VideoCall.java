package com.example.owlpost_2_0.Controllers;

public class VideoCall {
    public static void main(String[] args) {
        String remoteIP = "192.168.230.28";//friend er ip
        int port = 5000;

        new Thread(() -> {
            try {
                VideoSender.start(remoteIP, port);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                VideoReceiver.start(port);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}