package com.example.owlpost_2_0.Server;


import com.example.owlpost_2_0.ChatRoom.ChatMessage;
import com.example.owlpost_2_0.Database.DatabaseHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

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

    private void setupStreams() throws Exception{
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }

    private void readUsername() throws Exception{
        this.username = (String) in.readObject();
        System.out.println(username + " connected.");
        DatabaseHandler.getInstance().updateOnlineStatus(username, true);
        Server.broadcastStatusChange(username, true);
        ChatMessage statusMsg = new ChatMessage("SYSTEM", "ALL", "USER_ONLINE:" + username);
        Server.broadcast(statusMsg);
    }

    private void listenForMsg() {
        while (!socket.isClosed()) {
            try {
                if(in.readObject() instanceof ChatMessage msg){
                    if (msg.getReceiver().startsWith("GROUP:")) {
                        Server.broadcast(msg);
                    }
                    else{
                        System.out.println("Received message: " + msg);
                        Server.broadcast(msg);
                    }
                }
            }catch (Exception e) {
                break;
            }
        }
    }


    public String getUsername() {
        return username;
    }

    private void cleanup() {
        try {
            Server.removeClient(this);
            if (username != null) {
                DatabaseHandler.getInstance().updateOnlineStatus(username, false);
                Server.broadcastStatusChange(username, false);
                ChatMessage statusMsg = new ChatMessage("SYSTEM", "ALL", "USER_OFFLINE:" + username);
                Server.broadcast(statusMsg);
            }
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("User disconnected: " + username);
        } catch (IOException e) {
            System.out.println("Error during cleanup: " + e.getMessage());
        }
    }

    public void send(ChatMessage msg) {
        try {
            if (socket.isClosed() || out == null) {
                cleanup();
                return;
            }
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            System.out.println("Error sending message to " + username + ": " + e.getMessage());
            cleanup();
        }
    }
}