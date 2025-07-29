package com.example.owlpost_2_0.Controllers;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class ClientUDP {
    private static volatile boolean isRunning = false;
    private static DatagramSocket socket;
    private static TargetDataLine mic;
    private static SourceDataLine speakers;
    private static Thread senderThread;
    private static Thread receiverThread;
    private static volatile boolean isMuted = false;

    public static void start(String relayIP, int relayPortPara) {
        if (isRunning) {
            stop();
        }

        System.out.println("UDP Audio Client starting...");

        try {
            socket = new DatagramSocket();
            socket.setSendBufferSize(2048);
            socket.setReceiveBufferSize(2048);

            InetAddress relayAddr = InetAddress.getByName(relayIP);
            int relayPort = relayPortPara;
            AudioFormat format = new AudioFormat(16000f, 16, 1, true, false);
            mic = AudioSystem.getTargetDataLine(format);
            mic.open(format, 4096);
            mic.start();
            speakers = AudioSystem.getSourceDataLine(format);
            speakers.open(format, 4096);
            speakers.start();

            isRunning = true;
            System.out.println("Audio components initialized successfully");
            senderThread = new Thread(() -> {
                byte[] buffer = new byte[1024];
                try {
                    while (isRunning && !Thread.currentThread().isInterrupted()) {
                        if (!isMuted) {
                            int bytesRead = mic.read(buffer, 0, buffer.length);
                            if (bytesRead > 0) {
                                DatagramPacket packet = new DatagramPacket(
                                        Arrays.copyOf(buffer, bytesRead),
                                        bytesRead,
                                        relayAddr,
                                        relayPort
                                );
                                socket.send(packet);
                            }
                        } else {
                            Arrays.fill(buffer, (byte) 0);
                            DatagramPacket packet = new DatagramPacket(
                                    buffer,
                                    buffer.length,
                                    relayAddr,
                                    relayPort
                            );
                            socket.send(packet);
                        }
                        Thread.sleep(10);
                    }
                } catch (IOException | InterruptedException e) {
                    if (isRunning) {
                        System.err.println("Audio sender error: " + e.getMessage());
                    }
                } finally {
                    System.out.println("Audio sender thread stopped");
                }
            }, "Audio-Sender");
            receiverThread = new Thread(() -> {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                try {
                    while (isRunning && !Thread.currentThread().isInterrupted()) {
                        socket.receive(packet);
                        if (speakers.isOpen()) {
                            speakers.write(packet.getData(), 0, packet.getLength());
                        }
                        packet.setLength(buffer.length);
                    }
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("Audio receiver error: " + e.getMessage());
                    }
                } finally {
                    System.out.println("Audio receiver thread stopped");
                }
            }, "Audio-Receiver");
            senderThread.setPriority(Thread.MAX_PRIORITY);
            receiverThread.setPriority(Thread.MAX_PRIORITY);
            senderThread.start();
            receiverThread.start();

            System.out.println("Audio call started successfully");

        } catch (LineUnavailableException | IOException e) {
            System.err.println("Failed to start audio call: " + e.getMessage());
            stop();
        }
    }

    public static void stop() {
        System.out.println("Stopping audio call...");
        isRunning = false;
        if (senderThread != null && senderThread.isAlive()) {
            senderThread.interrupt();
        }
        if (receiverThread != null && receiverThread.isAlive()) {
            receiverThread.interrupt();
        }
        try {
            if (mic != null) {
                mic.stop();
                mic.close();
                mic = null;
            }
            if (speakers != null) {
                speakers.stop();
                speakers.close();
                speakers = null;
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }
        } catch (Exception e) {
            System.err.println("Error during audio cleanup: " + e.getMessage());
        }

        System.out.println("Audio call stopped");
    }

    public static void setMuted(boolean muted) {
        isMuted = muted;
        System.out.println("Audio " + (muted ? "muted" : "unmuted"));
    }

    public static boolean isMuted() {
        return isMuted;
    }

    public static boolean isRunning() {
        return isRunning;
    }
}