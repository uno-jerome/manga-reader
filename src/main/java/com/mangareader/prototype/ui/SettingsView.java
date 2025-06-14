package com.mangareader.prototype.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class SettingsView extends StackPane {
    public SettingsView() {
        Label label = new Label("Settings View");
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: #888;");
        getChildren().add(label);
    }
}