package com.example.owlpost_2_0.Game;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

public class TicTacToeGame {
    private Stage gameStage;
    private GridPane gameGrid;
    private Button[][] gameButtons;
    private Label statusLabel;
    private Label playerInfoLabel;
    private GameClient gameClient;
    private String gameId;
    private String playerSymbol;  // 'X' or 'O'
    private String opponentName;
    private String currentPlayer;
    private int gridSize;
    private String[][] gameBoard;
    private boolean gameActive = true;
    private VBox gameContainer;
    private HBox controlButtons;

    public TicTacToeGame(GameClient gameClient, String gameId, int gridSize,
                         String playerSymbol, String opponentName) {
        this.gameClient = gameClient;
        this.gameId = gameId;
        this.gridSize = gridSize;
        this.playerSymbol = playerSymbol;
        this.opponentName = opponentName;
        this.currentPlayer = "X"; // X always starts

        initializeGame();
        setupGameStage();
    }

    private void initializeGame() {
        gameBoard = new String[gridSize][gridSize];
        gameButtons = new Button[gridSize][gridSize];

        // Initialize empty board
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                gameBoard[i][j] = "";
            }
        }
    }

    private void setupGameStage() {
        gameStage = new Stage();
        gameStage.initStyle(StageStyle.DECORATED);
        gameStage.setTitle("⚡ Magical TicTacToe - Duel of Minds ⚡");
        gameStage.setResizable(false);

        // Main container
        gameContainer = new VBox(20);
        gameContainer.setAlignment(Pos.CENTER);
        gameContainer.setPadding(new Insets(30));
        gameContainer.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #0d0707, #1a0f08, #2c1810);" +
                        "-fx-border-color: #8b4513;" +
                        "-fx-border-width: 3px;" +
                        "-fx-border-radius: 15px;" +
                        "-fx-background-radius: 15px;"
        );

        // Title
        Label titleLabel = new Label("⚔️ WIZARDING DUEL ⚔️");
        titleLabel.setStyle(
                "-fx-text-fill: #ffd700;" +
                        "-fx-font-family: 'Cinzel', 'Times New Roman', serif;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 24px;" +
                        "-fx-effect: dropshadow(gaussian, #000000, 10, 0.8, 0, 0);"
        );

        // Player info
        playerInfoLabel = new Label();
        updatePlayerInfo();
        playerInfoLabel.setStyle(
                "-fx-text-fill: #e6ddd4;" +
                        "-fx-font-family: 'Cinzel', 'Times New Roman', serif;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 16px;" +
                        "-fx-text-alignment: center;"
        );

        // Status label
        statusLabel = new Label("⏳ Waiting for your move...");
        statusLabel.setStyle(
                "-fx-text-fill: #90ee90;" +
                        "-fx-font-family: 'Times New Roman', serif;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-text-alignment: center;"
        );

        // Game grid
        createGameGrid();

        // Control buttons
        createControlButtons();

        gameContainer.getChildren().addAll(titleLabel, playerInfoLabel, statusLabel, gameGrid, controlButtons);

        Scene scene = new Scene(gameContainer);
        scene.setFill(Color.BLACK);
        gameStage.setScene(scene);

        // Add fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), gameContainer);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        gameStage.setOnCloseRequest(e -> {
            exitGame();
        });
    }

    private void updatePlayerInfo() {
        String info = String.format("🧙‍♂️ You (%s) vs 🧙‍♀️ %s (%s)",
                playerSymbol, opponentName,
                playerSymbol.equals("X") ? "O" : "X");
        playerInfoLabel.setText(info);
    }

    private void createGameGrid() {
        gameGrid = new GridPane();
        gameGrid.setAlignment(Pos.CENTER);
        gameGrid.setHgap(5);
        gameGrid.setVgap(5);
        gameGrid.setPadding(new Insets(20));
        gameGrid.setStyle(
                "-fx-background-color: rgba(26, 15, 8, 0.9);" +
                        "-fx-background-radius: 15px;" +
                        "-fx-border-color: #8b4513;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 15px;" +
                        "-fx-effect: innershadow(gaussian, #000000, 15, 0.7, 0, 0);"
        );

        int buttonSize = gridSize == 3 ? 80 : 60;

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                Button button = new Button();
                button.setPrefSize(buttonSize, buttonSize);
                button.setStyle(createButtonStyle(false));

                final int row = i;
                final int col = j;

                button.setOnAction(e -> makeMove(row, col));

                // Hover effects
                button.setOnMouseEntered(e -> {
                    if (button.getText().isEmpty() && gameActive && currentPlayer.equals(playerSymbol)) {
                        button.setStyle(createButtonStyle(true));
                    }
                });

                button.setOnMouseExited(e -> {
                    if (button.getText().isEmpty()) {
                        button.setStyle(createButtonStyle(false));
                    }
                });

                gameButtons[i][j] = button;
                gameGrid.add(button, j, i);
            }
        }
    }

    private String createButtonStyle(boolean hovered) {
        String baseColor = hovered ? "rgba(44, 24, 16, 0.9)" : "rgba(26, 15, 8, 0.8)";
        String borderColor = hovered ? "#d4af37" : "#8b4513";
        String textColor = "#ffd700";

        return String.format(
                "-fx-background-color: %s;" +
                        "-fx-border-color: %s;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-text-fill: %s;" +
                        "-fx-font-family: 'Times New Roman', serif;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 24px;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, #000000, 8, 0.5, 0, 2);",
                baseColor, borderColor, textColor
        );
    }

    private void createControlButtons() {
        controlButtons = new HBox(20);
        controlButtons.setAlignment(Pos.CENTER);

        Button playAgainBtn = new Button("🔄 Cast Again");
        playAgainBtn.setStyle(createControlButtonStyle("#0f2a0f", "#90ee90"));
        playAgainBtn.setOnAction(e -> playAgain());
        playAgainBtn.setVisible(false);

        Button exitBtn = new Button("🚪 Leave Duel");
        exitBtn.setStyle(createControlButtonStyle("#2a0a0a", "#ff6b6b"));
        exitBtn.setOnAction(e -> exitGame());

        controlButtons.getChildren().addAll(playAgainBtn, exitBtn);
    }

    private String createControlButtonStyle(String bgColor, String textColor) {
        return String.format(
                "-fx-background-color: linear-gradient(to bottom, %s, rgba(0,0,0,0.8));" +
                        "-fx-text-fill: %s;" +
                        "-fx-font-family: 'Cinzel', 'Times New Roman', serif;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 12 20;" +
                        "-fx-background-radius: 25px;" +
                        "-fx-border-color: %s;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 25px;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, #000000, 8, 0.6, 0, 3);",
                bgColor, textColor, textColor.replace("#", "rgba(").replace("rgb", "").replace(")", ", 0.8)")
        );
    }

    private void makeMove(int row, int col) {
        if (!gameActive || !currentPlayer.equals(playerSymbol) || !gameBoard[row][col].isEmpty()) {
            return;
        }

        // Send move to server
        gameClient.sendMove(gameId, row, col);
    }

    public void handleMove(int row, int col, String symbol) {
        Platform.runLater(() -> {
            if (row >= 0 && row < gridSize && col >= 0 && col < gridSize && gameBoard[row][col].isEmpty()) {
                gameBoard[row][col] = symbol;
                gameButtons[row][col].setText(symbol);

                // Style the button based on symbol
                String symbolColor = symbol.equals("X") ? "#ff6b6b" : "#4CAF50";
                gameButtons[row][col].setStyle(
                        gameButtons[row][col].getStyle() +
                                String.format("-fx-text-fill: %s; -fx-background-color: rgba(44, 24, 16, 0.9);", symbolColor)
                );

                // Add scale animation
                ScaleTransition scale = new ScaleTransition(Duration.millis(200), gameButtons[row][col]);
                scale.setFromX(0.8);
                scale.setFromY(0.8);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();

                // Switch current player
                currentPlayer = currentPlayer.equals("X") ? "O" : "X";
                updateStatus();

                // Check for win or draw
                checkGameEnd();
            }
        });
    }

    private void updateStatus() {
        if (gameActive) {
            if (currentPlayer.equals(playerSymbol)) {
                statusLabel.setText("⚡ Your turn - Cast your move!");
                statusLabel.setStyle(statusLabel.getStyle().replace("-fx-text-fill: #[^;]+", "-fx-text-fill: #90ee90"));
            } else {
                statusLabel.setText("⏳ " + opponentName + "'s turn...");
                statusLabel.setStyle(statusLabel.getStyle().replace("-fx-text-fill: #[^;]+", "-fx-text-fill: #ffd700"));
            }
        }
    }

    private void checkGameEnd() {
        String winner = checkWinner();
        if (winner != null) {
            gameActive = false;
            if (winner.equals(playerSymbol)) {
                statusLabel.setText("🎉 Victory! You have won the duel! 🎉");
                statusLabel.setStyle(statusLabel.getStyle().replace("-fx-text-fill: #[^;]+", "-fx-text-fill: #90ee90"));
            } else {
                statusLabel.setText("💀 Defeat! " + opponentName + " has won the duel!");
                statusLabel.setStyle(statusLabel.getStyle().replace("-fx-text-fill: #[^;]+", "-fx-text-fill: #ff6b6b"));
            }
            showEndGameButtons();
        } else if (isBoardFull()) {
            gameActive = false;
            statusLabel.setText("⚖️ The duel ends in a draw! Honor to both wizards!");
            statusLabel.setStyle(statusLabel.getStyle().replace("-fx-text-fill: #[^;]+", "-fx-text-fill: #ffd700"));
            showEndGameButtons();
        }
    }

    private String checkWinner() {
        // Check rows
        for (int i = 0; i < gridSize; i++) {
            if (checkLine(gameBoard[i])) {
                return gameBoard[i][0];
            }
        }

        // Check columns
        for (int j = 0; j < gridSize; j++) {
            String[] column = new String[gridSize];
            for (int i = 0; i < gridSize; i++) {
                column[i] = gameBoard[i][j];
            }
            if (checkLine(column)) {
                return column[0];
            }
        }

        // Check diagonals
        String[] diagonal1 = new String[gridSize];
        String[] diagonal2 = new String[gridSize];
        for (int i = 0; i < gridSize; i++) {
            diagonal1[i] = gameBoard[i][i];
            diagonal2[i] = gameBoard[i][gridSize - 1 - i];
        }

        if (checkLine(diagonal1)) {
            return diagonal1[0];
        }
        if (checkLine(diagonal2)) {
            return diagonal2[0];
        }

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

    private boolean isBoardFull() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (gameBoard[i][j].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void showEndGameButtons() {
        Platform.runLater(() -> {
            controlButtons.getChildren().get(0).setVisible(true); // Play again button
        });
    }

    private void playAgain() {
        gameClient.sendPlayAgain(gameId);
    }

    public void resetGame() {
        Platform.runLater(() -> {
            gameActive = true;
            currentPlayer = "X";

            // Clear board
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    gameBoard[i][j] = "";
                    gameButtons[i][j].setText("");
                    gameButtons[i][j].setStyle(createButtonStyle(false));
                }
            }

            updateStatus();
            controlButtons.getChildren().get(0).setVisible(false); // Hide play again button
        });
    }

    private void exitGame() {
        if (gameClient.isConnected()) {
            gameClient.sendExitGame(gameId);
        }
        closeGame();
    }

    public void closeGame() {
        Platform.runLater(() -> {
            if (gameStage != null) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(500), gameContainer);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> gameStage.close());
                fadeOut.play();
            }
        });
    }

    public void show() {
        Platform.runLater(() -> {
            gameStage.show();
            gameStage.toFront();
            updateStatus();
        });
    }

    public void handleGameMessage(String message) {
        Platform.runLater(() -> {
            if (message.equals("OPPONENT_LEFT")) {
                statusLabel.setText("💨 " + opponentName + " has left the duel!");
                statusLabel.setStyle(statusLabel.getStyle().replace("-fx-text-fill: #[^;]+", "-fx-text-fill: #ff6b6b"));
                gameActive = false;
                showEndGameButtons();
            }
        });
    }
}