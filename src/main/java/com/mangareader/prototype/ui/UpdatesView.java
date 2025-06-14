package com.mangareader.prototype.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class UpdatesView extends StackPane {
    public UpdatesView() {
        Label label = new Label("Updates View");
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: #888;");
        getChildren().add(label);
    }
}