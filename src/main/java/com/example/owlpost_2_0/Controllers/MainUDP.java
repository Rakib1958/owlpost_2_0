package com.example.owlpost_2_0.Controllers;

import java.io.IOException;
import java.net.*;

public class MainUDP {
    public static void main(String[] args) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(9806);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        System.out.println("UDP relay server started");

        InetAddress client1Addr = null;
        int client1Port = -1;
        InetAddress client2Addr = null;
        int client2Port = -1;

        byte[] buf = new byte[512];
        DatagramPacket pkt = new DatagramPacket(buf, buf.length);

        while (true) {
            try {
                socket.receive(pkt);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            InetAddress srcAddr = pkt.getAddress();
            int srcPort = pkt.getPort();

            if (client1Addr == null) {
                client1Addr = srcAddr;
                client1Port = srcPort;
                System.out.println("Client 1: " + client1Addr + ":" + client1Port);
            } else if (client2Addr == null && !(srcAddr.equals(client1Addr) && srcPort == client1Port)) {
                client2Addr = srcAddr;
                client2Port = srcPort;
                System.out.println("Client 2: " + client2Addr + ":" + client2Port);
            }

            InetAddress destAddr = null;
            int destPort = -1;
            if (srcAddr.equals(client1Addr) && srcPort == client1Port) {
                destAddr = client2Addr;
                destPort = client2Port;
            } else if (srcAddr.equals(client2Addr) && srcPort == client2Port) {
                destAddr = client1Addr;
                destPort = client1Port;
            }

            if (destAddr != null) {
                DatagramPacket forward = new DatagramPacket(pkt.getData(), pkt.getLength(), destAddr, destPort);
                try {
                    socket.send(forward);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            pkt.setLength(buf.length);
        }
    }
}