package com.mangareader.prototype.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class LibraryView extends StackPane {
    public LibraryView() {
        Label label = new Label("Library View (placeholder)");
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: #888;");
        getChildren().add(label);
    }
} 