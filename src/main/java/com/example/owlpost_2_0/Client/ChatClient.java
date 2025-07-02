package com.example.owlpost_2_0.Client;


import com.example.owlpost_2_0.ChatRoom.ChatMessage;
import javafx.application.Platform;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

public class ChatClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean isConnected = false;

    // connects to the server in the chatroom using the ip address and username
    public ChatClient(String serverIP, String username) throws Exception{
        socket = new Socket(serverIP, 1234);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        out.writeObject(username);
        out.flush();
        isConnected = true;
        System.out.println("Connected to server as " + username);
    }

    public void sendMessage(ChatMessage msg) throws Exception{
        out.writeObject(msg);
        out.flush();
    }

    // continuously listens for message in a different thread
    public void listenForMsg(Consumer<ChatMessage> messageHandler) {
        new Thread(() -> {
            try {
                while (true) {
                    try {
                        ChatMessage msg = (ChatMessage) in.readObject();
                        Platform.runLater(() -> messageHandler.accept(msg));
                    }catch (Exception e) {
                        break;
                    }
                }
            }catch (Exception e) {
                System.out.println("Disconnected from server");
                disconnect();
            }
        }).start();
    }

    public void disconnect() {
        try {
            isConnected = false;
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception e) {
            System.out.println("Error during disconnect: " + e.getMessage());
        }
    }
}
