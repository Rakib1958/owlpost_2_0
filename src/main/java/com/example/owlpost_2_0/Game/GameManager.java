package com.example.owlpost_2_0.Game;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import java.util.Optional;
import java.util.function.Consumer;

public class GameManager {
    private GameClient gameClient;
    private TicTacToeGame currentGame;
    private String playerName;
    private Consumer<String> gameRequestCallback;
    private static final String GAME_SERVER_HOST = "localhost";
    private static final int GAME_SERVER_PORT = 9900;

    public GameManager(String playerName) {
        this.playerName = playerName;
        initializeGameClient();
    }

    private void initializeGameClient() {
        gameClient = new GameClient(playerName);
        gameClient.setMessageHandler(this::handleServerMessage);
    }

    public boolean connectToGameServer() {
        return gameClient.connect(GAME_SERVER_HOST, GAME_SERVER_PORT);
    }

    public void sendGameRequest(String opponent, String gameType) {
        if (gameClient.isConnected()) {
            gameClient.sendGameRequest(opponent, gameType);
        } else {
            showAlert("Connection Error", "Not connected to game server!");
        }
    }

    public void setGameRequestCallback(Consumer<String> callback) {
        this.gameRequestCallback = callback;
    }

    private void handleServerMessage(String message) {
        System.out.println("Received game message: " + message);

        String[] parts = message.split(":");
        String messageType = parts[0];

        switch (messageType) {
            case "GAME_REQUEST":
                handleGameRequest(parts);
                break;
            case "GAME_STARTED":
                handleGameStarted(parts);
                break;
            case "MOVE":
                handleMove(parts);
                break;
            case "GAME_END":
                handleGameEnd(parts);
                break;
            case "PLAY_AGAIN_REQUEST":
                handlePlayAgainRequest(parts);
                break;
            case "GAME_RESET":
                handleGameReset();
                break;
            case "OPPONENT_LEFT":
                handleOpponentLeft();
                break;
            case "ERROR":
                handleError(parts);
                break;
            case "GAME_REQUEST_DECLINED":
                handleGameRequestDeclined(parts);
                break;
            default:
                System.out.println("Unknown game message type: " + messageType);
        }
    }

    private void handleGameRequest(String[] parts) {
        if (parts.length >= 4) {
            String requestId = parts[1];
            String fromPlayer = parts[2];
            String gameType = parts[3];

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("⚔️ Wizarding Duel Challenge!");
                alert.setHeaderText("🧙‍♂️ " + fromPlayer + " challenges you to a magical duel!");
                alert.setContentText("Game Type: " + gameType + " TicTacToe\n\nDo you accept this challenge?");
                alert.getDialogPane().setStyle(
                        "-fx-background-color: linear-gradient(to bottom, #122838, #364c52);" +
                                "-fx-border-color: #5e8581;" +
                                "-fx-border-width: 3px;" +
                                "-fx-border-radius: 15px;" +
                                "-fx-background-radius: 15px;"
                );
                alert.getDialogPane().lookup(".content.label").setStyle(
                        "-fx-text-fill: #f3f4d2;" +
                                "-fx-font-family: 'Times New Roman', serif;" +
                                "-fx-font-size: 14px;"
                );
                alert.getDialogPane().lookup(".header-panel .label").setStyle(
                        "-fx-text-fill: #a0b9a5;" +
                                "-fx-font-family: 'Cinzel', 'Times New Roman', serif;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 16px;"
                );

                ButtonType acceptBtn = new ButtonType("⚡ Accept Challenge");
                ButtonType declineBtn = new ButtonType("🛡️ Decline");
                alert.getButtonTypes().setAll(acceptBtn, declineBtn);
                alert.getDialogPane().lookupButton(acceptBtn).setStyle(
                        "-fx-background-color: #5e8581;" +
                                "-fx-text-fill: #f3f4d2;" +
                                "-fx-border-color: #a0b9a5;" +
                                "-fx-border-width: 2px;" +
                                "-fx-border-radius: 8px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-font-weight: bold;"
                );
                alert.getDialogPane().lookupButton(declineBtn).setStyle(
                        "-fx-background-color: #364c52;" +
                                "-fx-text-fill: #f3f4d2;" +
                                "-fx-border-color: #5e8581;" +
                                "-fx-border-width: 2px;" +
                                "-fx-border-radius: 8px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-font-weight: bold;"
                );

                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent() && result.get() == acceptBtn) {
                    gameClient.sendGameResponse(requestId, true);
                } else {
                    gameClient.sendGameResponse(requestId, false);
                }
            });
        }
    }

    private void handleGameStarted(String[] parts) {
        if (parts.length >= 5) {
            String gameId = parts[1];
            String gridSizeStr = parts[2];
            String playerSymbol = parts[3];
            String opponentName = parts[4];

            int gridSize = Integer.parseInt(gridSizeStr);

            Platform.runLater(() -> {
                currentGame = new TicTacToeGame(gameClient, gameId, gridSize, playerSymbol, opponentName);
                currentGame.show();
            });
        }
    }

    private void handleMove(String[] parts) {
        if (parts.length >= 5 && currentGame != null) {
            String gameId = parts[1];
            int row = Integer.parseInt(parts[2]);
            int col = Integer.parseInt(parts[3]);
            String symbol = parts[4];

            currentGame.handleMove(row, col, symbol);
        }
    }

    private void handleGameEnd(String[] parts) {
        if (parts.length >= 3 && currentGame != null) {
            String result = parts[2];
        }
    }

    private void handlePlayAgainRequest(String[] parts) {
        if (parts.length >= 2) {
            String fromPlayer = parts[1];

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("🔄 Another Round?");
                alert.setHeaderText("🧙‍♂️ " + fromPlayer + " wants to duel again!");
                alert.setContentText("Do you want to play another round?");
                alert.getDialogPane().setStyle(
                        "-fx-background-color: linear-gradient(to bottom, #122838, #364c52);" +
                                "-fx-border-color: #5e8581;" +
                                "-fx-border-width: 3px;" +
                                "-fx-border-radius: 15px;" +
                                "-fx-background-radius: 15px;"
                );
                alert.getDialogPane().lookup(".content.label").setStyle(
                        "-fx-text-fill: #f3f4d2;" +
                                "-fx-font-family: 'Times New Roman', serif;" +
                                "-fx-font-size: 14px;"
                );
                alert.getDialogPane().lookup(".header-panel .label").setStyle(
                        "-fx-text-fill: #a0b9a5;" +
                                "-fx-font-family: 'Cinzel', 'Times New Roman', serif;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 16px;"
                );

                ButtonType yesBtn = new ButtonType("⚡ Yes, Let's Duel!");
                ButtonType noBtn = new ButtonType("🚪 No, I'm Done");
                alert.getButtonTypes().setAll(yesBtn, noBtn);

                // Style the buttons
                alert.getDialogPane().lookupButton(yesBtn).setStyle(
                        "-fx-background-color: #5e8581;" +
                                "-fx-text-fill: #f3f4d2;" +
                                "-fx-border-color: #a0b9a5;" +
                                "-fx-border-width: 2px;" +
                                "-fx-border-radius: 8px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-font-weight: bold;"
                );
                alert.getDialogPane().lookupButton(noBtn).setStyle(
                        "-fx-background-color: #364c52;" +
                                "-fx-text-fill: #f3f4d2;" +
                                "-fx-border-color: #5e8581;" +
                                "-fx-border-width: 2px;" +
                                "-fx-border-radius: 8px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-font-weight: bold;"
                );

                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent() && result.get() == yesBtn) {
                } else {
                    if (currentGame != null) {
                        currentGame.closeGame();
                        currentGame = null;
                    }
                }
            });
        }
    }

    private void handleGameReset() {
        if (currentGame != null) {
            currentGame.resetGame();
        }
    }

    private void handleOpponentLeft() {
        if (currentGame != null) {
            currentGame.handleGameMessage("OPPONENT_LEFT");
        }
    }

    private void handleError(String[] parts) {
        if (parts.length >= 2) {
            String errorMessage = parts[1];
            Platform.runLater(() -> {
                showAlert("Game Error", errorMessage);
            });
        }
    }

    private void handleGameRequestDeclined(String[] parts) {
        if (parts.length >= 2) {
            String decliningPlayer = parts[1];
            Platform.runLater(() -> {
                showAlert("Challenge Declined",
                        "🛡️ " + decliningPlayer + " has declined your duel challenge.");
            });
        }
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.getDialogPane().setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #122838, #364c52);" +
                            "-fx-border-color: #5e8581;" +
                            "-fx-border-width: 3px;" +
                            "-fx-border-radius: 15px;" +
                            "-fx-background-radius: 15px;"
            );
            alert.getDialogPane().lookup(".content.label").setStyle(
                    "-fx-text-fill: #f3f4d2;" +
                            "-fx-font-family: 'Times New Roman', serif;" +
                            "-fx-font-size: 14px;"
            );

            alert.showAndWait();
        });
    }

    public void disconnect() {
        if (currentGame != null) {
            currentGame.closeGame();
            currentGame = null;
        }
        if (gameClient != null) {
            gameClient.disconnect();
        }
    }

    public boolean isConnected() {
        return gameClient != null && gameClient.isConnected();
    }
}