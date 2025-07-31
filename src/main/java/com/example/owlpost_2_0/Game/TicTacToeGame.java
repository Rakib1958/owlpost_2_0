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
    private static final String BASE_BUTTON_STYLE =
            "-fx-background-color: rgba(54, 76, 82, 0.9);" +
                    "-fx-border-color: #5e8581;" +
                    "-fx-border-width: 2px;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-text-fill: #f3f4d2;" +
                    "-fx-font-family: 'Times New Roman', serif;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 24px;" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, #122838, 8, 0.5, 0, 2);";

    private static final String HOVER_BUTTON_STYLE =
            "-fx-background-color: rgba(160, 185, 165, 0.9);" +
                    "-fx-border-color: #a0b9a5;" +
                    "-fx-border-width: 2px;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-text-fill: #122838;" +
                    "-fx-font-family: 'Times New Roman', serif;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 24px;" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, #122838, 8, 0.5, 0, 2);";

    public TicTacToeGame(GameClient gameClient, String gameId, int gridSize,
                         String playerSymbol, String opponentName) {
        this.gameClient = gameClient;
        this.gameId = gameId;
        this.gridSize = gridSize;
        this.playerSymbol = playerSymbol;
        this.opponentName = opponentName;
        this.currentPlayer = "X";

        initializeGame();
        setupGameStage();
    }

    private void initializeGame() {
        gameBoard = new String[gridSize][gridSize];
        gameButtons = new Button[gridSize][gridSize];

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

        gameContainer = new VBox(20);
        gameContainer.setAlignment(Pos.CENTER);
        gameContainer.setPadding(new Insets(30));
        gameContainer.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #122838, #364c52, #5e8581);" +
                        "-fx-border-color: #a0b9a5;" +
                        "-fx-border-width: 3px;" +
                        "-fx-border-radius: 15px;" +
                        "-fx-background-radius: 15px;"
        );

        Label titleLabel = new Label("⚔️ WIZARDING DUEL ⚔️");
        titleLabel.setStyle(
                "-fx-text-fill: #f3f4d2;" +
                        "-fx-font-family: 'Cinzel', 'Times New Roman', serif;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 24px;" +
                        "-fx-effect: dropshadow(gaussian, #122838, 10, 0.8, 0, 0);"
        );

        playerInfoLabel = new Label();
        updatePlayerInfo();
        playerInfoLabel.setStyle(
                "-fx-text-fill: #a0b9a5;" +
                        "-fx-font-family: 'Cinzel', 'Times New Roman', serif;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 16px;" +
                        "-fx-text-alignment: center;"
        );

        statusLabel = new Label("⏳ Waiting for your move...");
        statusLabel.setStyle(
                "-fx-text-fill: #f3f4d2;" +
                        "-fx-font-family: 'Times New Roman', serif;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-text-alignment: center;"
        );

        createGameGrid();
        createControlButtons();

        gameContainer.getChildren().addAll(titleLabel, playerInfoLabel, statusLabel, gameGrid, controlButtons);

        Scene scene = new Scene(gameContainer);
        scene.setFill(Color.web("#122838"));
        gameStage.setScene(scene);

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
                "-fx-background-color: rgba(18, 40, 56, 0.9);" +
                        "-fx-background-radius: 15px;" +
                        "-fx-border-color: #5e8581;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 15px;" +
                        "-fx-effect: innershadow(gaussian, #122838, 15, 0.7, 0, 0);"
        );

        int buttonSize = gridSize == 3 ? 80 : 60;

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                Button button = new Button();
                button.setPrefSize(buttonSize, buttonSize);
                button.setStyle(BASE_BUTTON_STYLE);

                final int row = i;
                final int col = j;

                button.setOnAction(e -> makeMove(row, col));

                button.setOnMouseEntered(e -> {
                    if (button.getText().isEmpty() && gameActive && currentPlayer.equals(playerSymbol)) {
                        button.setStyle(HOVER_BUTTON_STYLE);
                    }
                });

                button.setOnMouseExited(e -> {
                    if (button.getText().isEmpty()) {
                        button.setStyle(BASE_BUTTON_STYLE);
                    }
                });

                gameButtons[i][j] = button;
                gameGrid.add(button, j, i);
            }
        }
    }

    private void createControlButtons() {
        controlButtons = new HBox(20);
        controlButtons.setAlignment(Pos.CENTER);

        Button playAgainBtn = new Button("🔄 Cast Again");
        playAgainBtn.setStyle(createControlButtonStyle("#5e8581", "#f3f4d2"));
        playAgainBtn.setOnAction(e -> playAgain());
        playAgainBtn.setVisible(false);

        Button exitBtn = new Button("🚪 Leave Duel");
        exitBtn.setStyle(createControlButtonStyle("#364c52", "#f3f4d2"));
        exitBtn.setOnAction(e -> exitGame());

        controlButtons.getChildren().addAll(playAgainBtn, exitBtn);
    }

    private String createControlButtonStyle(String bgColor, String textColor) {
        return String.format(
                "-fx-background-color: linear-gradient(to bottom, %s, #122838);" +
                        "-fx-text-fill: %s;" +
                        "-fx-font-family: 'Cinzel', 'Times New Roman', serif;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 12 20;" +
                        "-fx-background-radius: 25px;" +
                        "-fx-border-color: #a0b9a5;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 25px;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, #122838, 8, 0.6, 0, 3);",
                bgColor, textColor
        );
    }

    private void makeMove(int row, int col) {
        if (!gameActive || !currentPlayer.equals(playerSymbol) || !gameBoard[row][col].isEmpty()) {
            return;
        }

        gameClient.sendMove(gameId, row, col);
    }

    public void handleMove(int row, int col, String symbol) {
        Platform.runLater(() -> {
            if (row >= 0 && row < gridSize && col >= 0 && col < gridSize && gameBoard[row][col].isEmpty()) {
                gameBoard[row][col] = symbol;
                gameButtons[row][col].setText(symbol);
                String symbolColor = symbol.equals("X") ? "#f3f4d2" : "#000000";
                String buttonStyle =
                        "-fx-background-color: rgba(54, 76, 82, 0.9);" +
                                "-fx-border-color: #5e8581;" +
                                "-fx-border-width: 2px;" +
                                "-fx-border-radius: 8px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-text-fill: " + symbolColor + ";" +
                                "-fx-font-family: 'Times New Roman', serif;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 24px;" +
                                "-fx-effect: dropshadow(gaussian, #122838, 8, 0.5, 0, 2);";

                gameButtons[row][col].setStyle(buttonStyle);
                gameButtons[row][col].setOnMouseEntered(null);
                gameButtons[row][col].setOnMouseExited(null);

                ScaleTransition scale = new ScaleTransition(Duration.millis(200), gameButtons[row][col]);
                scale.setFromX(0.8);
                scale.setFromY(0.8);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();

                currentPlayer = currentPlayer.equals("X") ? "O" : "X";
                updateStatus();

                checkGameEnd();
            }
        });
    }

    private void updateStatus() {
        if (gameActive) {
            if (currentPlayer.equals(playerSymbol)) {
                statusLabel.setText("⚡ Your turn - Cast your move!");
                statusLabel.setStyle(
                        "-fx-text-fill: #a0b9a5;" +
                                "-fx-font-family: 'Times New Roman', serif;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 14px;" +
                                "-fx-text-alignment: center;"
                );
            } else {
                statusLabel.setText("⏳ " + opponentName + "'s turn...");
                statusLabel.setStyle(
                        "-fx-text-fill: #f3f4d2;" +
                                "-fx-font-family: 'Times New Roman', serif;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 14px;" +
                                "-fx-text-alignment: center;"
                );
            }
        }
    }

    private void checkGameEnd() {
        String winner = checkWinner();
        if (winner != null) {
            gameActive = false;
            if (winner.equals(playerSymbol)) {
                statusLabel.setText("🎉 Victory! You have won the duel! 🎉");
                statusLabel.setStyle(
                        "-fx-text-fill: #a0b9a5;" +
                                "-fx-font-family: 'Times New Roman', serif;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 14px;" +
                                "-fx-text-alignment: center;"
                );
            } else {
                statusLabel.setText("💀 Defeat! " + opponentName + " has won the duel!");
                statusLabel.setStyle(
                        "-fx-text-fill: #5e8581;" +
                                "-fx-font-family: 'Times New Roman', serif;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 14px;" +
                                "-fx-text-alignment: center;"
                );
            }
            showEndGameButtons();
        } else if (isBoardFull()) {
            gameActive = false;
            statusLabel.setText("⚖️ The duel ends in a draw! Honor to both wizards!");
            statusLabel.setStyle(
                    "-fx-text-fill: #f3f4d2;" +
                            "-fx-font-family: 'Times New Roman', serif;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 14px;" +
                            "-fx-text-alignment: center;"
            );
            showEndGameButtons();
        }
    }

    private String checkWinner() {
        for (int i = 0; i < gridSize; i++) {
            if (checkLine(gameBoard[i])) {
                return gameBoard[i][0];
            }
        }

        for (int j = 0; j < gridSize; j++) {
            String[] column = new String[gridSize];
            for (int i = 0; i < gridSize; i++) {
                column[i] = gameBoard[i][j];
            }
            if (checkLine(column)) {
                return column[0];
            }
        }

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

            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    gameBoard[i][j] = "";
                    gameButtons[i][j].setText("");
                    gameButtons[i][j].setStyle(BASE_BUTTON_STYLE);

                    final int row = i;
                    final int col = j;
                    gameButtons[i][j].setOnMouseEntered(e -> {
                        if (gameButtons[row][col].getText().isEmpty() && gameActive && currentPlayer.equals(playerSymbol)) {
                            gameButtons[row][col].setStyle(HOVER_BUTTON_STYLE);
                        }
                    });

                    gameButtons[i][j].setOnMouseExited(e -> {
                        if (gameButtons[row][col].getText().isEmpty()) {
                            gameButtons[row][col].setStyle(BASE_BUTTON_STYLE);
                        }
                    });
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
                statusLabel.setStyle(
                        "-fx-text-fill: #5e8581;" +
                                "-fx-font-family: 'Times New Roman', serif;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 14px;" +
                                "-fx-text-alignment: center;"
                );
                gameActive = false;
                showEndGameButtons();
            }
        });
    }
}