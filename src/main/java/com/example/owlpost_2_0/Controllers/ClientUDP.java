package com.example.owlpost_2_0.Controllers;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class ClientUDP {
    public static void start(String relayIP, int relayPortPara) {
        System.out.println("UDP Client started");

        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSendBufferSize(1024);
            socket.setReceiveBufferSize(1024);

            InetAddress relayAddr = InetAddress.getByName(relayIP);//ekhane relay server er ip use hoy
            int relayPort = relayPortPara;

            AudioFormat format = new AudioFormat(8000f, 16, 1, true, false);
            TargetDataLine mic = AudioSystem.getTargetDataLine(format);
            mic.open(format);
            mic.start();

            SourceDataLine speakers = AudioSystem.getSourceDataLine(format);
            speakers.open(format);
            speakers.start();

            // Audio sender thread
            Thread sender = new Thread(() -> {
                try {
                    byte[] buf = new byte[512];
                    while (true) {
                        int len = mic.read(buf, 0, buf.length);
                        if (len > 0) {
                            DatagramPacket pkt = new DatagramPacket(Arrays.copyOf(buf, len), len, relayAddr, relayPort);
                            socket.send(pkt);
                            Thread.sleep(5); // optional pacing
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }, "Audio-Sender");
            sender.setPriority(Thread.MAX_PRIORITY);
            sender.start();

            // Audio receiver thread
            Thread receiver = new Thread(() -> {
                try {
                    byte[] buf = new byte[512];
                    DatagramPacket pkt = new DatagramPacket(buf, buf.length);
                    while (true) {
                        socket.receive(pkt);
                        speakers.write(pkt.getData(), 0, pkt.getLength());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, "Audio-Receiver");
            receiver.setPriority(Thread.MAX_PRIORITY);
            receiver.start();

            System.out.println("Press ENTER to stop...");
            System.in.read();

            mic.stop(); mic.close();
            speakers.stop(); speakers.close();
            socket.close();
        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }
}