package com.mangareader.prototype.ui;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mangareader.prototype.model.Chapter;
import com.mangareader.prototype.service.MangaService;
import com.mangareader.prototype.service.impl.DefaultMangaServiceImpl;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MangaReaderView extends StackPane {
    private final ScrollPane scrollPane;
    private final VBox pagesContainer;
    private final ProgressIndicator progressIndicator;
    private final Label errorLabel;

    private final MangaService mangaService;
    private final ExecutorService executorService;

    private Chapter currentChapter;
    private List<String> pageUrls;
    private int currentPageIndex;

    public MangaReaderView() {
        this.mangaService = new DefaultMangaServiceImpl();
        this.executorService = Executors.newSingleThreadExecutor();

        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: #333; -fx-background-color: #333;");

        pagesContainer = new VBox();
        pagesContainer.setAlignment(Pos.TOP_CENTER);
        pagesContainer.setSpacing(5);
        pagesContainer.setStyle("-fx-background-color: #333;");

        scrollPane.setContent(pagesContainer);

        progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(50, 50);
        progressIndicator.setVisible(false);

        errorLabel = new Label("Error loading pages.");
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
        errorLabel.setVisible(false);

        getChildren().addAll(scrollPane, progressIndicator, errorLabel);
        setAlignment(progressIndicator, Pos.CENTER);
        setAlignment(errorLabel, Pos.CENTER);
    }

    public void loadChapter(Chapter chapter) {
        if (chapter == null) {
            displayError("No chapter provided.");
            return;
        }

        this.currentChapter = chapter;
        pagesContainer.getChildren().clear();
        progressIndicator.setVisible(true);
        errorLabel.setVisible(false);

        executorService.submit(() -> {
            try {
                List<String> urls = mangaService.getChapterPages(chapter.getMangaId(), chapter.getId());
                Platform.runLater(() -> {
                    this.pageUrls = urls;
                    displayPages();
                    progressIndicator.setVisible(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    displayError("Failed to load pages: " + e.getMessage());
                    progressIndicator.setVisible(false);
                    e.printStackTrace();
                });
            }
        });
    }

    private void displayPages() {
        if (pageUrls == null || pageUrls.isEmpty()) {
            displayError("No pages found for this chapter.");
            return;
        }

        for (String url : pageUrls) {
            ImageView imageView = new ImageView();
            imageView.setFitWidth(1000);
            imageView.setPreserveRatio(true);

            Image image = new Image(url, true);
            imageView.setImage(image);

            pagesContainer.getChildren().add(imageView);
        }
        scrollPane.setVvalue(0);
    }

    private void displayError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        pagesContainer.getChildren().clear();
    }
}