package com.example.owlpost_2_0.Game;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class GameClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerName;
    private Consumer<String> messageHandler;
    private boolean isConnected = false;
    private Thread listenerThread;

    public GameClient(String playerName) {
        this.playerName = playerName;
    }

    public boolean connect(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send player name to server
            out.println("PLAYER:" + playerName);

            isConnected = true;

            // Start listening for messages
            listenerThread = new Thread(this::listenForMessages);
            listenerThread.setDaemon(true);
            listenerThread.start();

            System.out.println("Connected to game server");
            return true;

        } catch (IOException e) {
            System.err.println("Failed to connect to game server: " + e.getMessage());
            return false;
        }
    }

    public void setMessageHandler(Consumer<String> handler) {
        this.messageHandler = handler;
    }

    private void listenForMessages() {
        try {
            String message;
            while (isConnected && (message = in.readLine()) != null) {
                if (messageHandler != null) {
                    messageHandler.accept(message);
                }
            }
        } catch (IOException e) {
            if (isConnected) {
                System.err.println("Error reading from game server: " + e.getMessage());
            }
        }
    }

    public void sendMessage(String message) {
        if (out != null && isConnected) {
            out.println(message);
        }
    }

    public void sendGameRequest(String opponent, String gameType) {
        sendMessage("GAME_REQUEST:" + opponent + ":" + gameType);
    }

    public void sendGameResponse(String requestId, boolean accepted) {
        sendMessage("GAME_RESPONSE:" + requestId + ":" + (accepted ? "ACCEPT" : "DECLINE"));
    }

    public void sendMove(String gameId, int row, int col) {
        sendMessage("MOVE:" + gameId + ":" + row + ":" + col);
    }

    public void sendPlayAgain(String gameId) {
        sendMessage("PLAY_AGAIN:" + gameId);
    }

    public void sendExitGame(String gameId) {
        sendMessage("EXIT_GAME:" + gameId);
    }

    public void disconnect() {
        isConnected = false;
        try {
            if (listenerThread != null) {
                listenerThread.interrupt();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }
            System.out.println("Disconnected from game server");
        } catch (IOException e) {
            System.err.println("Error disconnecting from game server: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return isConnected && socket != null && !socket.isClosed();
    }

    public String getPlayerName() {
        return playerName;
    }
}