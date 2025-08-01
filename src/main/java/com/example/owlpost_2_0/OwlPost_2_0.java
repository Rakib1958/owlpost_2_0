package com.example.owlpost_2_0;

import com.example.owlpost_2_0.Resources.Animations;
import com.example.owlpost_2_0.Resources.Audios;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.Random;

public class OwlPost_2_0 extends Application {

    private Stage primaryStage;
    private Scene loginScene;
    private MediaPlayer mediaPlayer;
    @FXML
    private Label owlPostLabel;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        try {
            Font f = Font.loadFont(getClass().getResourceAsStream("Fonts/HARRYP__.TTF"), 12);
            Image icon = new Image(getClass().getResourceAsStream("Images/Icons/messenger-icon.png"));
            stage.getIcons().add(icon);
            stage.setTitle("OwlPost");
            stage.setResizable(false);
            showIntroVideo();

            stage.setOnCloseRequest(e->{
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Exit");
                alert.setHeaderText("Are you sure you want to leave hogwarts?");
                alert.setContentText("Press OK to exit");

                ButtonType button = alert.showAndWait().get();
                if (button == ButtonType.OK) {
                    // Stop any playing media
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                    }
                    Audios.stopAmbience();
                    System.exit(0);
                }else {
                    e.consume();
                }
            });
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void showIntroVideo() {
        try {
            StackPane videoRoot = new StackPane();
            videoRoot.setStyle("-fx-background-color: black;");
            Media media = loadVideoMedia();

            if (media != null) {
                mediaPlayer = new MediaPlayer(media);
                MediaView mediaView = new MediaView(mediaPlayer);
                mediaView.setFitWidth(1280);
                mediaView.setFitHeight(720);
                mediaView.setPreserveRatio(true);
                Label skipLabel = new Label("Press SPACE or Click to Skip");
                skipLabel.setTextFill(Color.WHITE);
                skipLabel.setStyle("-fx-font-family: 'Harry P'; -fx-font-size: 18px; -fx-background-color: rgba(0,0,0,0.7); -fx-padding: 10px; -fx-background-radius: 10px;");
                StackPane.setAlignment(skipLabel, Pos.BOTTOM_RIGHT);
                skipLabel.setOpacity(0);
                FadeTransition skipFade = new FadeTransition(Duration.millis(1000), skipLabel);
                skipFade.setFromValue(0);
                skipFade.setToValue(0.8);
                skipFade.setDelay(Duration.seconds(3));
                skipFade.play();

                videoRoot.getChildren().addAll(mediaView, skipLabel);
                Scene videoScene = new Scene(videoRoot, 1280, 720);
                videoScene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.SPACE) {
                        skipToLogin();
                    }
                });

                videoScene.setOnMouseClicked(event -> skipToLogin());
                mediaPlayer.setOnEndOfMedia(() -> {
                    Platform.runLater(this::startLoginScreen);
                });

                mediaPlayer.setOnError(() -> {
                    System.err.println("Video playback error: " + mediaPlayer.getError());
                    Platform.runLater(this::startLoginScreen);
                });
                primaryStage.setScene(videoScene);
                primaryStage.show();
                mediaPlayer.play();

            } else {
                System.out.println("No intro video found, proceeding to login screen");
                startLoginScreen();
            }

        } catch (Exception e) {
            System.err.println("Error loading intro video: " + e.getMessage());
            e.printStackTrace();
            startLoginScreen();
        }
    }

    private Media loadVideoMedia() {
        String[] videoPaths = {
                "/com/example/owlpost_2_0/Videos/OwlPost.mp4",
                "/Videos/intro.mp4",
                "/com/example/owlpost_2_0/Videos/intro.mov",
                "/Videos/intro.mov",
                "/com/example/owlpost_2_0/Videos/hogwarts_intro.mp4",
                "/Videos/hogwarts_intro.mp4"
        };

        for (String path : videoPaths) {
            try {
                var resource = getClass().getResource(path);
                if (resource != null) {
                    System.out.println("Found video at: " + path);
                    return new Media(resource.toExternalForm());
                }
            } catch (Exception e) {
                System.out.println("Could not load video from: " + path);
            }
        }
        String[] externalPaths = {
                "src/main/resources/Videos/intro.mp4",
                "Videos/intro.mp4",
                "intro.mp4"
        };

        for (String path : externalPaths) {
            try {
                File videoFile = new File(path);
                if (videoFile.exists()) {
                    System.out.println("Found external video at: " + path);
                    return new Media(videoFile.toURI().toString());
                }
            } catch (Exception e) {
                System.out.println("Could not load external video from: " + path);
            }
        }

        return null;
    }

    private void skipToLogin() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        startLoginScreen();
    }

    private void startLoginScreen() {
        try {
            Audios.playAmbience();
            Parent root = FXMLLoader.load(getClass().getResource("Fxml/loginform.fxml"));
            loginScene = new Scene(root);
            Image cursor = new Image(getClass().getResourceAsStream("Images/LoginForm/wand.png"), 128, 128, true, true);
            ImageCursor imageCursor = new ImageCursor(cursor);
            loginScene.setCursor(imageCursor);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), primaryStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(e -> {
                primaryStage.setScene(loginScene);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(800), loginScene.getRoot());
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });

            fadeOut.play();

        } catch (Exception e) {
            System.err.println("Error loading login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        launch();
    }
}