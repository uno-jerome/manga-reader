package com.mangareader.prototype;

import com.mangareader.prototype.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MangaReaderApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create the main view
        MainView mainView = new MainView();
        
        // Create the scene
        Scene scene = new Scene(mainView, 1280, 720);
        
        // Add CSS styling
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        
        // Set up the stage
        primaryStage.setTitle("MangaReader");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 