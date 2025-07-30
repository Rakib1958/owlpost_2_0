package com.example.owlpost_2_0;

import com.example.owlpost_2_0.Resources.Audios;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class OwlPost_2_0 extends Application {

    @Override
    public void start(Stage stage) {
        try {
            Font f = Font.loadFont(getClass().getResourceAsStream("Fonts/HARRYP__.TTF"), 12);

            Audios.playAmbience();
//            Audios.playSound("Ambience", 0);
            Parent root = FXMLLoader.load(getClass().getResource("Fxml/loginform.fxml"));
            Scene scene = new Scene(root);

            Image cursor = new Image(getClass().getResourceAsStream("Images/LoginForm/wand.png"), 128, 128, true, true);
            ImageCursor imageCursor = new ImageCursor(cursor);
            scene.setCursor(imageCursor);

            Image icon = new Image(getClass().getResourceAsStream("Images/Icons/messenger-icon.png"));
            stage.getIcons().add(icon);
            stage.setTitle("OwlPost");
            stage.setResizable(false);

            stage.setScene(scene);
            stage.show();

            stage.setOnCloseRequest(e->{
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Exit");
                alert.setHeaderText("Are you sure you want to leave hogwarts?");
                alert.setContentText("Press OK to exit");

                ButtonType button = alert.showAndWait().get();
                if (button == ButtonType.OK) {
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




    public static void main(String[] args) {
        launch();
    }
}