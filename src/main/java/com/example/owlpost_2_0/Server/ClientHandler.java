package com.example.owlpost_2_0.Server;


import com.example.owlpost_2_0.ChatRoom.ChatMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String username;

    // constructor accepts socket
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    // gets input and output stream and takes the username from client
    @Override
    public void run() {
        try {
            setupStreams();
            readUsername();
            listenForMsg();
        } catch (Exception e) {
            System.out.println("User disconnected: " + username);
        }finally {
            cleanup();
        }
    }

    // setup streams
    private void setupStreams() throws Exception{
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }

    // get username from client whenever they are connected
    private void readUsername() throws Exception{
        this.username = (String) in.readObject(); // First message is username
        System.out.println(username + " connected.");
    }

    // continuously reads message until the server is closed and wraps it in a ChatMessage class
    private void listenForMsg() {
        while (!socket.isClosed()) {
            try {
                ChatMessage msg = (ChatMessage) in.readObject();
                System.out.println("Received message: " + msg);
                Server.broadcast(msg);
            }catch (Exception e) {
                break;
            }
        }
    }

    public String getUsername() {
        return username;
    }

    // gracefully disconnect
    private void cleanup() {
        try {
            Server.removeClient(this);
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("User disconnected: " + username);
        } catch (IOException e) {
            System.out.println("Error during cleanup: " + e.getMessage());
        }
    }

    // write a ChatMessage as object into the output stream
    public void send(ChatMessage msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            System.out.println("Error sending message to " + username + ": " + e.getMessage());
            cleanup();
        }
    }
}