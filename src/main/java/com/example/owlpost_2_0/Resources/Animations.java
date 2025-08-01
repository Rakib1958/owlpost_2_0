package com.example.owlpost_2_0.Resources;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.util.Duration;


public class Animations {
    public static void FadeTransition(Pane pane, boolean show) {
        FadeTransition ft = new FadeTransition(Duration.millis(50), pane);
        if (show) {
            pane.setVisible(true);
            ft.setFromValue(0);
            ft.setToValue(1);
        } else {
            ft.setFromValue(1);
            ft.setToValue(0);
            ft.setOnFinished(event -> pane.setVisible(show));
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
                TranslateTransition translate = new TranslateTransition(Duration.seconds(1), label);
//                translate.setFromX(label.getLayoutX());
                translate.setFromX(0);
//                translate.setToX(label.getLayoutX() + 20);
                translate.setToX(20);
                translate.setAutoReverse(true);
                translate.setCycleCount(TranslateTransition.INDEFINITE);
                translate.play();
            }
            if (node instanceof Hyperlink) {
                Hyperlink link = (Hyperlink) node;
                TranslateTransition translate = new TranslateTransition(Duration.seconds(1), link);
//                translate.setFromX(label.getLayoutX());
                translate.setFromX(0);
//                translate.setToX(label.getLayoutX() + 20);
                translate.setToX(20);
                translate.setAutoReverse(true);
                translate.setCycleCount(TranslateTransition.INDEFINITE);
                translate.play();
            }
        }
    }

    public static void typeWriterEffect(Label label, String fullText, double typingSpeed) {
        label.setText("");

        Timeline timeline = new Timeline();
        for (int i = 0; i <= fullText.length(); i++) {
            final int index = i;
            KeyFrame keyFrame = new KeyFrame(
                    Duration.millis(typingSpeed * i),
                    e -> {
                        if (index <= fullText.length()) {
                            label.setText(fullText.substring(0, index));
                        }
                    }
            );
            timeline.getKeyFrames().add(keyFrame);
        }

        timeline.play();
    }

    public static void typeWriterEffect(Label label, String fullText) {
        typeWriterEffect(label, fullText, 150);
    }

    public static void typeWriterEffectWithCursor(Label label, String fullText, double typingSpeed, boolean showCursor) {
        label.setText(""); // Clear the label initially

        Timeline typingTimeline = new Timeline();
        Timeline cursorTimeline = new Timeline();
        for (int i = 0; i <= fullText.length(); i++) {
            final int index = i;
            KeyFrame keyFrame = new KeyFrame(
                    Duration.millis(typingSpeed * i),
                    e -> {
                        String currentText = fullText.substring(0, index);
                        if (showCursor && index < fullText.length()) {
                            label.setText(currentText + "|");
                        } else {
                            label.setText(currentText);
                        }
                    }
            );
            typingTimeline.getKeyFrames().add(keyFrame);
        }
        if (showCursor) {
            KeyFrame cursorBlink = new KeyFrame(
                    Duration.millis(500),
                    e -> {
                        String currentText = label.getText();
                        if (currentText.endsWith("|")) {
                            label.setText(currentText.substring(0, currentText.length() - 1));
                        } else if (!currentText.equals(fullText)) {
                            label.setText(currentText + "|");
                        }
                    }
            );
            cursorTimeline.getKeyFrames().add(cursorBlink);
            cursorTimeline.setCycleCount(Timeline.INDEFINITE);
        }
        typingTimeline.play();
        if (showCursor) {
            cursorTimeline.play();
            typingTimeline.setOnFinished(e -> {
                cursorTimeline.stop();
                label.setText(fullText);
            });
        }
    }

}