package com.example.owlpost_2_0.Server;

import com.example.owlpost_2_0.ChatRoom.ChatMessage;
import com.example.owlpost_2_0.Database.DatabaseHandler;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {
    private ServerSocket serverSocket;
    private static Set<ClientHandler> clients = new HashSet<>();

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void start() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("A new client has joined...");
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                new Thread(handler).start();
            }
        }catch (Exception e) {
            System.out.println("Server disconnected " + e.getMessage());
            close();
        }
    }

    public void close() {
        try {
            if (serverSocket != null || !serverSocket.isClosed()) {
                serverSocket.close();
            }
        }catch (Exception e) {
            System.out.println("Error closing server " + e.getMessage());
        }
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Client removed. Active clients : " + clients.size());
    }

    public static void broadcast(ChatMessage msg) {
        System.out.println("Broadcasting from " + msg.getSender() + " to " + msg.getReceiver());
        for (var client : clients) {
            if (client.getUsername().equals(msg.getReceiver()) ||
                    client.getUsername().equals(msg.getSender())) {
                client.send(msg);
            }
        }
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket1 = new ServerSocket(1234);
            Server server = new Server(serverSocket1);
            System.out.println("Server started...");

            Runtime.getRuntime().addShutdownHook(new Thread(()->{
                System.out.println("Shutting down server...");
                server.close();
            }));
            server.start();
        }catch (Exception e) {
            System.out.println("Error starting server " + e.getMessage());
        }
    }
}