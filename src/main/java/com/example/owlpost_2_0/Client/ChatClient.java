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
    private String username;

    // connects to the server in the chatroom using the ip address and username
    public ChatClient(String serverIP, String username) throws Exception{
        this.username = username;
        socket = new Socket(serverIP, 1234);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        out.writeObject(username);
        out.flush();
        isConnected = true;
        System.out.println("Connected to server as " + username);
    }

    public void sendMessage(ChatMessage msg) throws Exception{
        if (isConnected) {
            out.writeObject(msg);
            out.flush();
        }
    }

    // continuously listens for message in a different thread
    public void listenForMsg(Consumer<ChatMessage> messageHandler) {
        new Thread(() -> {
            try {
                while (isConnected) {
                    try {
                        ChatMessage msg = (ChatMessage) in.readObject();
                        Platform.runLater(() -> messageHandler.accept(msg));
                    } catch (Exception e) {
                        if (isConnected) {
                            System.out.println("Error reading message: " + e.getMessage());
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println("Disconnected from server");
                disconnect();
            }
        }).start();
    }

//    // Send call initiation signal
//    public void initiateCall(String receiverUsername, String callType) throws Exception {
//        if (isConnected) {
//            ChatMessage callMsg = new ChatMessage(username, receiverUsername, callType);
//            sendMessage(callMsg);
//        }
//    }
//
//    // Send call response signal
//    public void respondToCall(String callerUsername, String response) throws Exception {
//        if (isConnected) {
//            ChatMessage responseMsg = new ChatMessage(username, callerUsername, response);
//            sendMessage(responseMsg);
//        }
//    }
//
//    // Send call end signal
//    public void endCall(String otherUsername) throws Exception {
//        if (isConnected) {
//            ChatMessage endMsg = new ChatMessage(username, otherUsername, "CALL_ENDED");
//            sendMessage(endMsg);
//        }
//    }
//
//    // Get connection status
//    public boolean isConnected() {
//        return isConnected && socket != null && !socket.isClosed();
//    }
//
//    // Get username
//    public String getUsername() {
//        return username;
//    }

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