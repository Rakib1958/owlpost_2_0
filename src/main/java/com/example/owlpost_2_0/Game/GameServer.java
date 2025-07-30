package com.example.owlpost_2_0.Game;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameServer {
    private static final int PORT = 9900;
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private Map<String, GameSession> activeSessions = new ConcurrentHashMap<>();
    private Map<String, String> pendingRequests = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            System.out.println("🎮 Wizarding Game Server started on port " + PORT);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting game server: " + e.getMessage());
        }
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }

    public void registerClient(String playerName, ClientHandler handler) {
        clients.put(playerName, handler);
        System.out.println("Player registered: " + playerName);
    }

    public void unregisterClient(String playerName) {
        clients.remove(playerName);
        System.out.println("Player unregistered: " + playerName);
    }

    public void handleGameRequest(String requester, String opponent, String gameType) {
        if (!clients.containsKey(opponent)) {
            clients.get(requester).sendMessage("ERROR:Opponent not found or offline");
            return;
        }

        String requestId = UUID.randomUUID().toString();
        pendingRequests.put(requestId, requester + ":" + opponent + ":" + gameType);

        clients.get(opponent).sendMessage("GAME_REQUEST:" + requestId + ":" + requester + ":" + gameType);
        System.out.println("Game request sent from " + requester + " to " + opponent + " (" + gameType + ")");
    }

    public void handleGameResponse(String requestId, boolean accepted, String responder) {
        String requestInfo = pendingRequests.remove(requestId);
        if (requestInfo == null) return;

        String[] parts = requestInfo.split(":");
        String requester = parts[0];
        String opponent = parts[1];
        String gameType = parts[2];

        if (!accepted) {
            if (clients.containsKey(requester)) {
                clients.get(requester).sendMessage("GAME_REQUEST_DECLINED:" + responder);
            }
            return;
        }
        String gameId = UUID.randomUUID().toString();
        int gridSize = gameType.equals("3x3") ? 3 : 4;
        boolean requesterIsX = Math.random() < 0.5;
        String requesterSymbol = requesterIsX ? "X" : "O";
        String responderSymbol = requesterIsX ? "O" : "X";

        GameSession session = new GameSession(gameId, requester, opponent, gridSize);
        activeSessions.put(gameId, session);
        clients.get(requester).sendMessage("GAME_STARTED:" + gameId + ":" + gridSize + ":" +
                requesterSymbol + ":" + opponent);
        clients.get(responder).sendMessage("GAME_STARTED:" + gameId + ":" + gridSize + ":" +
                responderSymbol + ":" + requester);

        System.out.println("Game started: " + requester + " vs " + opponent + " (" + gameType + ")");
    }

    public void handleMove(String gameId, int row, int col, String player) {
        GameSession session = activeSessions.get(gameId);
        if (session == null) return;

        if (session.makeMove(row, col, player)) {
            String symbol = session.getPlayerSymbol(player);
            session.getPlayers().forEach(p -> {
                if (clients.containsKey(p)) {
                    clients.get(p).sendMessage("MOVE:" + gameId + ":" + row + ":" + col + ":" + symbol);
                }
            });

            // Check game end
            String winner = session.checkWinner();
            if (winner != null || session.isBoardFull()) {
                String result = winner != null ? "WIN" : "DRAW";
                session.getPlayers().forEach(p -> {
                    if (clients.containsKey(p)) {
                        clients.get(p).sendMessage("GAME_END:" + gameId + ":" + winner + ":" + result);
                    }
                });
            }
        }
    }

    public void handlePlayAgain(String gameId, String player) {
        GameSession session = activeSessions.get(gameId);
        if (session == null) return;

        String opponent = session.getOpponent(player);
        if (clients.containsKey(opponent)) {
            clients.get(opponent).sendMessage("PLAY_AGAIN_REQUEST:" + player);

            // Reset game
            session.reset();
            session.getPlayers().forEach(p -> {
                if (clients.containsKey(p)) {
                    clients.get(p).sendMessage("GAME_RESET:" + gameId);
                }
            });
        }
    }

    public void handleExitGame(String gameId, String player) {
        GameSession session = activeSessions.remove(gameId);
        if (session == null) return;

        String opponent = session.getOpponent(player);
        if (clients.containsKey(opponent)) {
            clients.get(opponent).sendMessage("OPPONENT_LEFT");
        }

        System.out.println("Game ended: " + gameId);
    }
    public static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String playerName;
        private GameServer server;

        public ClientHandler(Socket socket, GameServer server) {
            this.socket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    processMessage(inputLine);
                }
            } catch (IOException e) {
                System.err.println("Client handler error: " + e.getMessage());
            } finally {
                cleanup();
            }
        }

        private void processMessage(String message) {
            String[] parts = message.split(":");
            String command = parts[0];

            switch (command) {
                case "PLAYER":
                    if (parts.length >= 2) {
                        playerName = parts[1];
                        server.registerClient(playerName, this);
                    }
                    break;
                case "GAME_REQUEST":
                    if (parts.length >= 3) {
                        server.handleGameRequest(playerName, parts[1], parts[2]);
                    }
                    break;
                case "GAME_RESPONSE":
                    if (parts.length >= 3) {
                        boolean accepted = parts[2].equals("ACCEPT");
                        server.handleGameResponse(parts[1], accepted, playerName);
                    }
                    break;
                case "MOVE":
                    if (parts.length >= 4) {
                        String gameId = parts[1];
                        int row = Integer.parseInt(parts[2]);
                        int col = Integer.parseInt(parts[3]);
                        server.handleMove(gameId, row, col, playerName);
                    }
                    break;
                case "PLAY_AGAIN":
                    if (parts.length >= 2) {
                        server.handlePlayAgain(parts[1], playerName);
                    }
                    break;
                case "EXIT_GAME":
                    if (parts.length >= 2) {
                        server.handleExitGame(parts[1], playerName);
                    }
                    break;
            }
        }

        public void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }

        private void cleanup() {
            try {
                if (playerName != null) {
                    server.unregisterClient(playerName);
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error cleaning up client: " + e.getMessage());
            }
        }
    }
    public static class GameSession {
        private String gameId;
        private String player1;
        private String player2;
        private int gridSize;
        private String[][] board;
        private String currentPlayer;
        private Map<String, String> playerSymbols;

        public GameSession(String gameId, String player1, String player2, int gridSize) {
            this.gameId = gameId;
            this.player1 = player1;
            this.player2 = player2;
            this.gridSize = gridSize;
            this.board = new String[gridSize][gridSize];
            this.currentPlayer = "X";
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    board[i][j] = "";
                }
            }
            playerSymbols = new HashMap<>();
            if (Math.random() < 0.5) {
                playerSymbols.put(player1, "X");
                playerSymbols.put(player2, "O");
            } else {
                playerSymbols.put(player1, "O");
                playerSymbols.put(player2, "X");
            }
        }

        public boolean makeMove(int row, int col, String player) {
            if (row < 0 || row >= gridSize || col < 0 || col >= gridSize) return false;
            if (!board[row][col].isEmpty()) return false;
            if (!playerSymbols.get(player).equals(currentPlayer)) return false;

            board[row][col] = currentPlayer;
            currentPlayer = currentPlayer.equals("X") ? "O" : "X";
            return true;
        }

        public String checkWinner() {
            for (int i = 0; i < gridSize; i++) {
                if (checkLine(board[i])) {
                    return board[i][0];
                }
            }
            for (int j = 0; j < gridSize; j++) {
                String[] column = new String[gridSize];
                for (int i = 0; i < gridSize; i++) {
                    column[i] = board[i][j];
                }
                if (checkLine(column)) {
                    return column[0];
                }
            }
            String[] diagonal1 = new String[gridSize];
            String[] diagonal2 = new String[gridSize];
            for (int i = 0; i < gridSize; i++) {
                diagonal1[i] = board[i][i];
                diagonal2[i] = board[i][gridSize - 1 - i];
            }

            if (checkLine(diagonal1)) return diagonal1[0];
            if (checkLine(diagonal2)) return diagonal2[0];

            return null;
        }

        private boolean checkLine(String[] line) {
            if (line[0].isEmpty()) return false;
            for (int i = 1; i < line.length; i++) {
                if (!line[i].equals(line[0])) {
                    return false;
                }
            }
            return true;
        }

        public boolean isBoardFull() {
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    if (board[i][j].isEmpty()) {
                        return false;
                    }
                }
            }
            return true;
        }

        public void reset() {
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    board[i][j] = "";
                }
            }
            currentPlayer = "X";
        }

        public List<String> getPlayers() {
            return Arrays.asList(player1, player2);
        }

        public String getOpponent(String player) {
            return player.equals(player1) ? player2 : player1;
        }

        public String getPlayerSymbol(String player) {
            return playerSymbols.get(player);
        }
    }
}