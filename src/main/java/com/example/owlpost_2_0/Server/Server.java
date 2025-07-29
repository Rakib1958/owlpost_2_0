package com.example.owlpost_2_0.Server;

import com.example.owlpost_2_0.ChatRoom.ChatMessage;
import com.example.owlpost_2_0.Database.DatabaseHandler;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private ServerSocket serverSocket;
    private static Set<ClientHandler> clients = new HashSet<>();
    private static Map<String, Set<String>> groupMembers = new ConcurrentHashMap<>();

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

    public static HashSet getOnlineList(){
        return new HashSet(clients);
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Client removed. Active clients : " + clients.size());
    }

    public static void broadcast(ChatMessage msg) {
        System.out.println("Broadcasting from " + msg.getSender() + " to " + msg.getReceiver());
        var clientsCopy = new ArrayList<>(clients);
        if (msg.getReceiver().startsWith("GROUP:")) {
            String groupId = msg.getReceiver().substring(6);
            broadcastToGroup(groupId, msg);
            return;
        }
        if (msg.getReceiver().startsWith("REGISTER_GROUP:")) {
            String groupId = msg.getReceiver().substring(15);
            addUserToGroup(msg.getSender(), groupId);
            return;
        }
        for (var client : clientsCopy) {
            if (client.getUsername().equals(msg.getReceiver()) ||
                    client.getUsername().equals(msg.getSender())) {
                client.send(msg);
            }
        }
    }

    public static void broadcastStatusChange(String username, boolean isOnline) {
        ChatMessage statusMsg = new ChatMessage("SYSTEM", "ALL",
                isOnline ? "USER_ONLINE:" + username : "USER_OFFLINE:" + username);
        var clientsCopy = new ArrayList<>(clients);
        for (var client : clientsCopy) {
            if (!isOnline && client.getUsername().equals(username)) {
                continue;
            }
            client.send(statusMsg);
        }
    }

    public static void broadcastToGroup(String groupId, ChatMessage msg) {
        Set<String> members = groupMembers.get(groupId);
        if (members == null) {
            // Load group members from database
            members = DatabaseHandler.getInstance().getGroupMembers(groupId);
            if (members != null) {
                groupMembers.put(groupId, members);
            }
        }

        if (members != null) {
            var clientsCopy = new ArrayList<>(clients);
            for (var client : clientsCopy) {
                if (members.contains(client.getUsername())) {
                    client.send(msg);
                }
            }
        }
    }

    public static void handleGroupMessage(String groupId, String senderUsername, String content) {
        // Create a special ChatMessage for group communication
        ChatMessage groupMsg = new ChatMessage(senderUsername, "GROUP:" + groupId, content);
        broadcastToGroup(groupId, groupMsg);
    }


    public static void addUserToGroup(String username, String groupId) {
        groupMembers.computeIfAbsent(groupId, k -> ConcurrentHashMap.newKeySet()).add(username);
    }

    public static void removeUserFromGroup(String username, String groupId) {
        Set<String> members = groupMembers.get(groupId);
        if (members != null) {
            members.remove(username);
            if (members.isEmpty()) {
                groupMembers.remove(groupId);
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