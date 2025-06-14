package com.mangareader.prototype.ui;

import java.util.List;

import com.mangareader.prototype.model.Manga;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class MangaListView extends ScrollPane {
    private final FlowPane mangaGrid;
    private final Label statusLabel;

    public MangaListView() {
        // Create the main container
        mangaGrid = new FlowPane();
        mangaGrid.setHgap(20);
        mangaGrid.setVgap(20);
        mangaGrid.setPadding(new Insets(20));

        // Create status label
        statusLabel = new Label("No manga found");
        statusLabel.setTextAlignment(TextAlignment.CENTER);

        // Set up the scroll pane
        setContent(mangaGrid);
        setFitToWidth(true);
        setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    public void displayManga(List<Manga> mangaList) {
        mangaGrid.getChildren().clear();

        if (mangaList.isEmpty()) {
            mangaGrid.getChildren().add(statusLabel);
            return;
        }

        for (Manga manga : mangaList) {
            VBox mangaCard = createMangaCard(manga);
            mangaGrid.getChildren().add(mangaCard);
        }
    }

    private VBox createMangaCard(Manga manga) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(10));
        card.setMaxWidth(150);
        card.setStyle("-fx-background-color: white; -fx-border-color: lightgray; -fx-border-radius: 5;");

        // Cover image
        ImageView coverImage = new ImageView();
        coverImage.setFitWidth(130);
        coverImage.setFitHeight(180);
        coverImage.setPreserveRatio(true);
        if (manga.getCoverUrl() != null) {
            try {
                coverImage.setImage(new Image(manga.getCoverUrl(), true));
            } catch (Exception e) {
                coverImage.setImage(new Image("placeholder.png"));
            }
        }

        // Title
        Label titleLabel = new Label(manga.getTitle());
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(130);
        titleLabel.setStyle("-fx-font-weight: bold;");

        // Status
        Label statusLabel = new Label(manga.getStatus());
        statusLabel.setStyle("-fx-text-fill: gray;");

        card.getChildren().addAll(coverImage, titleLabel, statusLabel);

        // Add click handler
        card.setOnMouseClicked(e -> {
            System.out.println("Selected manga: " + manga.getTitle());
        });

        return card;
    }
}