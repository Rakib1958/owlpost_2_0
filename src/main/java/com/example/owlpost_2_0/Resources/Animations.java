package com.example.owlpost_2_0.Resources;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.w3c.dom.Text;


public class Animations {
    public static void FadeTransition(Node node, boolean show) {
        FadeTransition ft = new FadeTransition(Duration.millis(1000), node);
        if (show) {
            node.setVisible(true);
            ft.setFromValue(0);
            ft.setToValue(1);
        } else {
            ft.setFromValue(1);
            ft.setToValue(0);
            ft.setOnFinished(event -> node.setVisible(show));
        }
        ft.play();
    }

    public static void TranslateRight(Pane pane) {
        TranslateTransition translate = new TranslateTransition(Duration.millis(1000), pane);
        translate.setByX(0);
        translate.setToX(1280);
        translate.setCycleCount(1);
        translate.setAutoReverse(false);
        translate.play();
    }

    public static void TranslateLeft(Pane pane) {
        TranslateTransition translate = new TranslateTransition(Duration.millis(1000), pane);
        translate.setByX(1280);
        translate.setToX(0);
        translate.setCycleCount(1);
        translate.setAutoReverse(false);
        translate.play();
    }

    public static void leftRight(Pane pane) {
        for (var node : pane.getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
//                TranslateTransition translate = new TranslateTransition(Duration.seconds(1), label);
////                translate.setFromX(label.getLayoutX());
//                translate.setFromX(0);
////                translate.setToX(label.getLayoutX() + 20);
//                translate.setToX(20);
//                translate.setAutoReverse(true);
//                translate.setCycleCount(TranslateTransition.INDEFINITE);
//                translate.play();
                playTypewriterEffectLabel(label, label.getText(), Duration.millis(100));
            }
            if (node instanceof Hyperlink) {
                Hyperlink link = (Hyperlink) node;
//                TranslateTransition translate = new TranslateTransition(Duration.seconds(1), link);
////                translate.setFromX(label.getLayoutX());
//                translate.setFromX(0);
////                translate.setToX(label.getLayoutX() + 20);
//                translate.setToX(20);
//                translate.setAutoReverse(true);
//                translate.setCycleCount(TranslateTransition.INDEFINITE);
//                translate.play();
                playTypewriterEffectHyper(link, link.getText(), Duration.millis(100));
            }
            if (node instanceof TextField) {
                TextField textField = (TextField) node;
//                TranslateTransition translate = new TranslateTransition(Duration.seconds(1), link);
////                translate.setFromX(label.getLayoutX());
//                translate.setFromX(0);
////                translate.setToX(label.getLayoutX() + 20);
//                translate.setToX(20);
//                translate.setAutoReverse(true);
//                translate.setCycleCount(TranslateTransition.INDEFINITE);
//                translate.play();
                playTypewriterEffectText(textField, textField.getPromptText(), Duration.millis(100));

            }
        }
    }

    public static void playTypewriterEffectLabel(Label label, String message, Duration delayPerChar) {
        label.setVisible(true); // make sure it's visible
        Timeline timeline = new Timeline();

        for (int i = 0; i < message.length(); i++) {
            final int index = i;
            KeyFrame keyFrame = new KeyFrame(delayPerChar.multiply(i), e -> {
                label.setText(message.substring(0, index + 1));
            });
            timeline.getKeyFrames().add(keyFrame);
        }
        timeline.setOnFinished(e -> {
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(ev -> playTypewriterEffectLabel(label, label.getText(), Duration.millis(100)));
            pause.play();
        });

        timeline.play();
    }

    public static void playTypewriterEffectHyper(Hyperlink hyperlink, String message, Duration delayPerChar) {
        hyperlink.setVisible(true); // make sure it's visible
        Timeline timeline = new Timeline();

        for (int i = 0; i < message.length(); i++) {
            final int index = i;
            KeyFrame keyFrame = new KeyFrame(delayPerChar.multiply(i), e -> {
                hyperlink.setText(message.substring(0, index + 1));
            });
            timeline.getKeyFrames().add(keyFrame);
        }
        timeline.setOnFinished(e -> {
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(ev -> playTypewriterEffectHyper(hyperlink, hyperlink.getText(), Duration.millis(100)));
            pause.play();
        });

        timeline.play();
    }

    public static void playTypewriterEffectText(TextField textField, String message, Duration delayPerChar) {
        textField.setVisible(true); // make sure it's visible
        Timeline timeline = new Timeline();

        for (int i = 0; i < message.length(); i++) {
            final int index = i;
            KeyFrame keyFrame = new KeyFrame(delayPerChar.multiply(i), e -> {
                textField.setPromptText(message.substring(0, index + 1));
            });
            timeline.getKeyFrames().add(keyFrame);
        }
        timeline.setOnFinished(e -> {
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(ev -> playTypewriterEffectText(textField, textField.getPromptText(), Duration.millis(100)));
            pause.play();
        });

        timeline.play();
    }

    public static void ScaleTransition(Node node) {
        ScaleTransition pop = new ScaleTransition(Duration.seconds(3), node);
        pop.setFromX(0.8);
        pop.setToX(1.0);
        pop.setFromY(0.8);
        pop.setToY(1.0);
        pop.setCycleCount(Animation.INDEFINITE);
        pop.setAutoReverse(true);
        pop.play();
    }


}