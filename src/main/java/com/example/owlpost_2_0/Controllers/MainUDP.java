package com.example.owlpost_2_0.Controllers;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

public class MainUDP {
    private static volatile boolean isRunning = false;
    private static DatagramSocket socket;
    private static Thread serverThread;
    private static ConcurrentHashMap<String, ClientInfo> clients = new ConcurrentHashMap<>();

    static class ClientInfo {
        InetAddress address;
        int port;
        long lastSeen;

        ClientInfo(InetAddress address, int port) {
            this.address = address;
            this.port = port;
            this.lastSeen = System.currentTimeMillis();
        }

        void updateLastSeen() {
            this.lastSeen = System.currentTimeMillis();
        }
    }

    public static void start(int port) {
        if (isRunning) {
            stop();
        }

        try {
            socket = new DatagramSocket(port);
            socket.setReceiveBufferSize(4096);
            socket.setSendBufferSize(4096);
            isRunning = true;

            System.out.println("UDP Audio Relay Server started on port " + port);

            serverThread = new Thread(() -> {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while (isRunning && !Thread.currentThread().isInterrupted()) {
                    try {
                        socket.receive(packet);

                        InetAddress srcAddr = packet.getAddress();
                        int srcPort = packet.getPort();
                        String clientKey = srcAddr.getHostAddress() + ":" + srcPort;

                        clients.compute(clientKey, (key, existingClient) -> {
                            if (existingClient == null) {
                                System.out.println("New client connected: " + key);
                                return new ClientInfo(srcAddr, srcPort);
                            } else {
                                existingClient.updateLastSeen();
                                return existingClient;
                            }
                        });
                        for (ClientInfo client : clients.values()) {
                            if (!(client.address.equals(srcAddr) && client.port == srcPort)) {
                                try {
                                    DatagramPacket forwardPacket = new DatagramPacket(
                                            packet.getData(),
                                            packet.getLength(),
                                            client.address,
                                            client.port
                                    );
                                    socket.send(forwardPacket);
                                } catch (IOException e) {
                                    System.err.println("Error forwarding to client: " + e.getMessage());
                                }
                            }
                        }
                        packet.setLength(buffer.length);

                    } catch (IOException e) {
                        if (isRunning) {
                            System.err.println("Server error: " + e.getMessage());
                        }
                    }
                }
            }, "Audio-Relay-Server");

            serverThread.start();

        } catch (SocketException e) {
            System.err.println("Failed to start audio relay server: " + e.getMessage());
        }
    }

    public static void stop() {
        System.out.println("Stopping audio relay server...");
        isRunning = false;

        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt();
        }

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }

        clients.clear();
        System.out.println("Audio relay server stopped");
    }

    public static void main(String[] args) {
        start(9806);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            stop();
        }));
    }
}