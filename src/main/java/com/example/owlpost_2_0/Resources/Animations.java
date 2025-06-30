package com.example.owlpost_2_0.Resources;

import javafx.animation.FadeTransition;
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


}
