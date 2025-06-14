package com.mangareader.prototype.ui;

import java.util.Arrays;
import java.util.Optional;

import com.mangareader.prototype.model.Manga;
import com.mangareader.prototype.service.MangaService;
import com.mangareader.prototype.service.impl.DefaultMangaServiceImpl;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AddSeriesModal extends Dialog<Manga> {

    private final MangaService mangaService;
    private Manga mangaToAdd;

    private final ImageView coverImageView;
    private final TextField titleField;
    private final TextArea descriptionArea;
    private final TextField authorsField;
    private final TextField artistsField;
    private final FlowPane tagsFlowPane;
    private final TextField addTagField;
    private final ComboBox<String> languageComboBox;
    private final ComboBox<String> statusComboBox;
    private TextField coverUrlField; // Added coverUrlField as a class member

    public AddSeriesModal(Manga manga) {
        this.mangaService = new DefaultMangaServiceImpl();
        this.mangaToAdd = manga;

        // Debug output for manga data
        System.out.println("Creating AddSeriesModal with manga data:");
        System.out.println("  ID: " + manga.getId());
        System.out.println("  Title: " + manga.getTitle());
        System.out.println("  Author: " + manga.getAuthor());
        System.out.println("  Artist: " + manga.getArtist());

        setTitle("View Series");
        setHeaderText(null); // No default header text

        // Setup dialog pane
        DialogPane dialogPane = getDialogPane();
        dialogPane.getStyleClass().add("add-series-dialog");
        dialogPane.setPrefWidth(900);
        dialogPane.setPrefHeight(700);
        dialogPane.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());

        // --- UI Components --- //

        // Cover Image
        coverImageView = new ImageView();
        coverImageView.setFitWidth(200);
        coverImageView.setFitHeight(300);
        coverImageView.setPreserveRatio(true);
        coverImageView.setStyle(
                "-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");

        // Always set a placeholder first
        String placeholderUrl = "https://via.placeholder.com/200x300/f8f9fa/6c757d?text=No+Cover";
        Image placeholderImage = new Image(placeholderUrl, true);

        // Add error handling even for placeholder images
        placeholderImage.errorProperty().addListener((obs, wasError, isError) -> {
            if (isError) {
                System.err.println("Error loading placeholder image, using fallback");
                // Use a simple fallback if even placeholder fails
                Platform.runLater(() -> {
                    try {
                        Image fallbackImage = new Image(
                                "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjMwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZjhmOWZhIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxNiIgZmlsbD0iIzZjNzU3ZCIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPk5vIENvdmVyPC90ZXh0Pjwvc3ZnPg==");
                        coverImageView.setImage(fallbackImage);
                    } catch (Exception e) {
                        System.err.println("All image loading failed: " + e.getMessage());
                    }
                });
            }
        });

        coverImageView.setImage(placeholderImage);

        // Load cover image if available
        loadCoverImage(manga);

        // Title Field
        titleField = new TextField(manga.getTitle());
        titleField.setPromptText("Title");
        titleField.setPrefColumnCount(30);
        titleField.setEditable(false); // Make title read-only
        titleField.setFocusTraversable(false);
        titleField.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-text-fill: #495057;");

        // Description Area
        descriptionArea = new TextArea(manga.getDescription());
        descriptionArea.setPromptText("Description");
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(6);
        descriptionArea.setPrefColumnCount(30);
        descriptionArea.setEditable(false); // Make description read-only
        descriptionArea.setFocusTraversable(false);
        descriptionArea.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-text-fill: #495057;");

        // Authors Field
        String authorText = manga.getAuthor() != null ? manga.getAuthor() : "";
        authorsField = new TextField(authorText);
        authorsField.setPromptText("Author(s)");
        authorsField.setPrefColumnCount(30);
        authorsField.setStyle(
                "-fx-background-color: white; -fx-border-color: #007bff; -fx-border-width: 2; -fx-prompt-text-fill: #6c757d;");
        System.out.println("Setting author field text: " + authorText);

        // Artists Field
        String artistText = manga.getArtist() != null ? manga.getArtist() : "";
        artistsField = new TextField(artistText);
        artistsField.setPromptText("Artist(s)");
        artistsField.setPrefColumnCount(30);
        artistsField.setStyle(
                "-fx-background-color: white; -fx-border-color: #007bff; -fx-border-width: 2; -fx-prompt-text-fill: #6c757d;");
        System.out.println("Setting artist field text: " + artistText);

        // Tags FlowPane (for displaying chips)
        tagsFlowPane = new FlowPane(8, 8);
        tagsFlowPane.setPadding(new Insets(10));

        tagsFlowPane.setPrefWrapLength(400);
        tagsFlowPane.setPrefHeight(60);
        tagsFlowPane.setMinHeight(60);
        tagsFlowPane.setMaxWidth(Double.MAX_VALUE);
        if (manga.getGenres() != null) {
            manga.getGenres().forEach(this::addTagChip);
        }
        tagsFlowPane.setDisable(true); // Make tags field non-editable

        // Wrap tagsFlowPane in a ScrollPane
        ScrollPane tagsScrollPane = new ScrollPane(tagsFlowPane);
        tagsScrollPane.setFitToWidth(true);
        tagsScrollPane.setPrefHeight(120); // Limit height to prevent overflow
        tagsScrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        tagsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tagsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Initialize detailsGrid before adding components
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(15);
        detailsGrid.setVgap(20);
        detailsGrid.setAlignment(Pos.TOP_LEFT);

        // Add Tag Field
        addTagField = new TextField();
        addTagField.setPromptText("Add Tags (comma-separated)");
        addTagField.setOnAction(event -> {
            String text = addTagField.getText().trim();
            if (!text.isEmpty()) {
                Arrays.stream(text.split(",")).map(String::trim).filter(s -> !s.isEmpty()).forEach(this::addTagChip);
                addTagField.clear();
            }
        });
        addTagField.setVisible(false); // Hide add tag field when tags are read-only

        // Language ComboBox
        languageComboBox = new ComboBox<>();
        languageComboBox.getItems().addAll("English", "Japanese", "Korean", "Chinese");
        languageComboBox.setValue(manga.getLanguage() != null ? manga.getLanguage() : "English");
        languageComboBox.setPromptText("Language");
        languageComboBox.setStyle("-fx-background-color: white; -fx-border-color: #ced4da;");

        // Status ComboBox
        statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Ongoing", "Completed", "Hiatus", "Cancelled");
        statusComboBox.setValue(manga.getStatus() != null ? manga.getStatus() : "Ongoing");
        statusComboBox.setPromptText("Status");
        statusComboBox.setStyle("-fx-background-color: white; -fx-border-color: #ced4da;");

        // --- Layout --- //
        GridPane contentGrid = new GridPane();
        contentGrid.setHgap(40);
        contentGrid.setVgap(15);
        contentGrid.setPadding(new Insets(30, 40, 30, 40));
        contentGrid.setAlignment(Pos.CENTER);

        // Left side: Cover Image and URL
        VBox leftColumn = new VBox(20);
        leftColumn.setAlignment(Pos.TOP_CENTER);

        // Cover image container with title
        VBox coverContainer = new VBox(10);
        coverContainer.setAlignment(Pos.CENTER);
        Label coverLabel = new Label("Cover Image");
        coverLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #495057;");
        coverContainer.getChildren().addAll(coverLabel, coverImageView);

        // Create coverUrlField as a class-level field so it can be updated from image
        // loading methods
        coverUrlField = new TextField(manga.getCoverUrl() != null ? manga.getCoverUrl() : "");
        coverUrlField.setEditable(false);
        coverUrlField.setFocusTraversable(false);
        coverUrlField.setPromptText("Cover URL");
        coverUrlField.setStyle(
                "-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-text-fill: #6c757d; -fx-font-size: 11px;");

        leftColumn.getChildren().addAll(coverContainer, coverUrlField);
        leftColumn.setPrefWidth(250);
        leftColumn.setMaxWidth(250);

        // Right side: Details fields
        // Create styled labels
        Label titleLabel = new Label("Title");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        Label descLabel = new Label("Description");
        descLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        Label authorLabel = new Label("Author(s)");
        authorLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #007bff;");
        Label artistLabel = new Label("Artist(s)");
        artistLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #007bff;");
        Label tagsLabel = new Label("Tags");
        tagsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        Label langLabel = new Label("Language");
        langLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        Label statusLabel = new Label("Status");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");

        detailsGrid.add(titleLabel, 0, 0);
        detailsGrid.add(titleField, 1, 0);
        detailsGrid.add(descLabel, 0, 1);
        detailsGrid.add(descriptionArea, 1, 1);
        detailsGrid.add(authorLabel, 0, 2);
        detailsGrid.add(authorsField, 1, 2);
        detailsGrid.add(artistLabel, 0, 3);
        detailsGrid.add(artistsField, 1, 3);
        detailsGrid.add(tagsLabel, 0, 4);
        detailsGrid.add(tagsScrollPane, 1, 4);
        detailsGrid.add(langLabel, 0, 5);
        detailsGrid.add(languageComboBox, 1, 5);
        detailsGrid.add(statusLabel, 0, 6);
        detailsGrid.add(statusComboBox, 1, 6);

        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(90);
        labelCol.setMaxWidth(120);
        labelCol.setHgrow(Priority.NEVER);
        ColumnConstraints inputCol = new ColumnConstraints();
        inputCol.setHgrow(Priority.ALWAYS);
        detailsGrid.getColumnConstraints().addAll(labelCol, inputCol);

        contentGrid.add(leftColumn, 0, 0);
        contentGrid.add(detailsGrid, 1, 0);
        GridPane.setVgrow(detailsGrid, Priority.ALWAYS);
        GridPane.setHgrow(detailsGrid, Priority.ALWAYS);

        dialogPane.setContent(contentGrid);

        // --- Buttons --- //
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType addSeriesButtonType = new ButtonType("View Series", ButtonBar.ButtonData.APPLY);
        dialogPane.getButtonTypes().addAll(cancelButtonType, addSeriesButtonType);

        // Button actions
        setResultConverter(dialogButton -> {
            if (dialogButton == addSeriesButtonType) {
                updateMangaObject();
                mangaService.addToLibrary(mangaToAdd);
                return mangaToAdd;
            }
            return null;
        });
    }

    private void addTagChip(String tagText) {
        Label tagLabel = new Label(tagText);
        tagLabel.getStyleClass().add("tag-chip");
        tagLabel.setStyle(
                "-fx-background-color: #e7f3ff; -fx-padding: 6 12; -fx-background-radius: 18; -fx-font-size: 12px; -fx-text-fill: #0066cc; -fx-border-color: #b3d9ff; -fx-border-width: 1; -fx-border-radius: 18;");
        // Remove close button for read-only tags
        HBox tagChip = new HBox(5, tagLabel);
        tagChip.setAlignment(Pos.CENTER_LEFT);
        tagChip.getStyleClass().add("tag-chip-container");
        tagsFlowPane.getChildren().add(tagChip);
    }

    private void updateMangaObject() {
        // Do not update title, description, or genres (tags) since they are read-only
        mangaToAdd.setAuthor(authorsField.getText());
        mangaToAdd.setArtist(artistsField.getText());
        mangaToAdd.setLanguage(languageComboBox.getValue());
        mangaToAdd.setStatus(statusComboBox.getValue());
    }

    public Optional<Manga> showAndAwaitResult() {
        // Try to get full manga details before showing the dialog
        if (mangaToAdd != null && mangaToAdd.getId() != null && !mangaToAdd.getId().isEmpty()) {
            // Check if we need to fetch author/artist data
            if ((mangaToAdd.getAuthor() == null || mangaToAdd.getAuthor().isEmpty()) ||
                    (mangaToAdd.getArtist() == null || mangaToAdd.getArtist().isEmpty())) {

                System.out.println("Fetching complete manga details before showing dialog");
                try {
                    // Try to get full details synchronously to ensure data is available when dialog
                    // opens
                    Optional<Manga> fullManga = mangaService.getMangaDetails(mangaToAdd.getId());
                    if (fullManga.isPresent()) {
                        Manga completeData = fullManga.get();

                        // Update author field if needed
                        if (completeData.getAuthor() != null && !completeData.getAuthor().isEmpty()) {
                            mangaToAdd.setAuthor(completeData.getAuthor());
                            authorsField.setText(completeData.getAuthor());
                            System.out.println("Pre-dialog: Updated author to " + completeData.getAuthor());
                        }

                        // Update artist field if needed
                        if (completeData.getArtist() != null && !completeData.getArtist().isEmpty()) {
                            mangaToAdd.setArtist(completeData.getArtist());
                            artistsField.setText(completeData.getArtist());
                            System.out.println("Pre-dialog: Updated artist to " + completeData.getArtist());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error fetching complete manga details: " + e.getMessage());
                }
            }
        }

        // Show the dialog and return the result
        Optional<Manga> result = showAndWait();
        // Only return the result if it's present (i.e., user clicked "Add Series")
        // If result is empty, it means user cancelled or closed the dialog
        return result;
    }

    /**
     * Loads the cover image for a manga with fallbacks for error cases.
     * If the initial cover URL fails, tries to fetch it directly from the source.
     *
     * @param manga The manga to load the cover for
     */
    private void loadCoverImage(Manga manga) {
        String placeholderUrl = "https://via.placeholder.com/200x300/f8f9fa/6c757d?text=No+Cover";
        // Always set placeholder first
        coverImageView.setImage(new Image(placeholderUrl, true));

        // Check if there's a valid cover URL
        if (manga.getCoverUrl() == null || manga.getCoverUrl().isEmpty()) {
            System.out.println("No cover URL found, fetching from source");
            tryFallbackCoverUrl(manga, placeholderUrl);
            return;
        }

        System.out.println("Loading cover from URL: " + manga.getCoverUrl());
        try {
            Image coverImg = new Image(manga.getCoverUrl(), true);

            // Handle load errors
            coverImg.exceptionProperty().addListener((obs, oldEx, newEx) -> {
                if (newEx != null) {
                    System.err.println("Error loading cover image: " + newEx.getMessage());
                    tryFallbackCoverUrl(manga, placeholderUrl);
                }
            });

            // Handle image error
            coverImg.errorProperty().addListener((obs, wasError, isError) -> {
                if (isError) {
                    System.err.println("Image error loading cover");
                    tryFallbackCoverUrl(manga, placeholderUrl);
                } else if (!isError && coverImg.getProgress() == 1.0) {
                    // Image successfully loaded
                    coverImageView.setImage(coverImg);
                    System.out.println("Cover image successfully loaded");
                }
            });

            // Add progress listener to set the image when fully loaded
            coverImg.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                if (newProgress.doubleValue() == 1.0 && !coverImg.isError()) {
                    coverImageView.setImage(coverImg);
                    System.out.println("Cover image loaded from progress listener");
                }
            });

        } catch (Exception e) {
            System.err.println("Exception during image loading: " + e.getMessage());
            tryFallbackCoverUrl(manga, placeholderUrl);
        }
    }

    /**
     * Attempts to fetch a cover URL directly from the manga source as a fallback
     *
     * @param manga          The manga to load the cover for
     * @param placeholderUrl URL to use if fallback also fails
     */
    private void tryFallbackCoverUrl(Manga manga, String placeholderUrl) {
        // Try to get the cover URL directly from the source
        if (manga.getId() != null && !manga.getId().isEmpty()) {
            System.out.println("Trying to fetch fallback cover URL for manga ID: " + manga.getId());
            new Thread(() -> {
                try {
                    // First try to get full manga details, which should include cover and
                    // author/artist info
                    mangaService.getMangaDetails(manga.getId()).ifPresentOrElse(
                            fullManga -> {
                                String newCoverUrl = null;

                                // Update cover URL if available
                                if (fullManga.getCoverUrl() != null && !fullManga.getCoverUrl().isEmpty()) {
                                    newCoverUrl = fullManga.getCoverUrl();
                                    manga.setCoverUrl(newCoverUrl);
                                    System.out.println("Retrieved cover URL from full manga details: " + newCoverUrl);
                                }

                                // Update author if available
                                if (fullManga.getAuthor() != null && !fullManga.getAuthor().isEmpty()) {
                                    String author = fullManga.getAuthor();
                                    manga.setAuthor(author);
                                    System.out.println("Retrieved author from full manga details: " + author);
                                    // Update the author field on the UI thread
                                    javafx.application.Platform.runLater(() -> {
                                        if (authorsField != null) {
                                            authorsField.setText(author);
                                        }
                                    });
                                }

                                // Update artist if available
                                if (fullManga.getArtist() != null && !fullManga.getArtist().isEmpty()) {
                                    String artist = fullManga.getArtist();
                                    manga.setArtist(artist);
                                    System.out.println("Retrieved artist from full manga details: " + artist);
                                    // Update the artist field on the UI thread
                                    javafx.application.Platform.runLater(() -> {
                                        if (artistsField != null) {
                                            artistsField.setText(artist);
                                        }
                                    });
                                }

                                // Update the cover URL field and load image if we have a new cover URL
                                if (newCoverUrl != null) {
                                    final String finalCoverUrl = newCoverUrl;

                                    // Update the cover URL field on the UI thread
                                    javafx.application.Platform.runLater(() -> {
                                        if (coverUrlField != null) {
                                            coverUrlField.setText(finalCoverUrl);
                                        }
                                    });

                                    // Load the new image on UI thread
                                    javafx.application.Platform.runLater(() -> {
                                        try {
                                            Image newCoverImg = new Image(finalCoverUrl, true);
                                            newCoverImg.progressProperty()
                                                    .addListener((obs, oldProgress, newProgress) -> {
                                                        if (newProgress.doubleValue() == 1.0
                                                                && !newCoverImg.isError()) {
                                                            coverImageView.setImage(newCoverImg);
                                                            System.out.println(
                                                                    "Fallback cover image successfully loaded");
                                                        }
                                                    });

                                            newCoverImg.errorProperty().addListener((obs, wasError, isError) -> {
                                                if (isError) {
                                                    System.err
                                                            .println("Error loading fallback cover, using placeholder");
                                                    coverImageView.setImage(new Image(placeholderUrl, true));
                                                }
                                            });
                                        } catch (Exception e) {
                                            System.err.println("Error loading fallback image: " + e.getMessage());
                                            coverImageView.setImage(new Image(placeholderUrl, true));
                                        }
                                    });
                                }
                            },
                            // If no details available, try direct cover URL method
                            () -> fallbackToDirectCoverUrl(manga, placeholderUrl));
                } catch (Exception e) {
                    System.err.println("Error in fallback cover process: " + e.getMessage());
                    javafx.application.Platform
                            .runLater(() -> coverImageView.setImage(new Image(placeholderUrl, true)));
                }
            }).start();
        } else {
            // No manga ID to get cover, use placeholder
            coverImageView.setImage(new Image(placeholderUrl, true));
        }
    }

    /**
     * Helper method to fetch cover URL directly as a last resort
     * 
     * @param manga          The manga to get cover for
     * @param placeholderUrl The placeholder to use if all fails
     */
    private void fallbackToDirectCoverUrl(Manga manga, String placeholderUrl) {
        try {
            String directCoverUrl = mangaService.getCoverUrl(manga.getId());
            if (directCoverUrl != null && !directCoverUrl.isEmpty()) {
                manga.setCoverUrl(directCoverUrl);
                System.out.println("Retrieved direct cover URL: " + directCoverUrl);

                // Update the cover URL field on the UI thread
                javafx.application.Platform.runLater(() -> {
                    if (coverUrlField != null) {
                        coverUrlField.setText(directCoverUrl);
                    }
                });

                // Load the new image on UI thread
                javafx.application.Platform.runLater(() -> {
                    try {
                        Image directCoverImg = new Image(directCoverUrl, true);
                        directCoverImg.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                            if (newProgress.doubleValue() == 1.0 && !directCoverImg.isError()) {
                                coverImageView.setImage(directCoverImg);
                                System.out.println("Direct cover image successfully loaded");
                            }
                        });

                        directCoverImg.errorProperty().addListener((obs, wasError, isError) -> {
                            if (isError) {
                                System.err.println("Error loading direct cover, using placeholder");
                                coverImageView.setImage(new Image(placeholderUrl, true));
                            }
                        });
                    } catch (Exception e) {
                        System.err.println("Error loading direct cover image: " + e.getMessage());
                        coverImageView.setImage(new Image(placeholderUrl, true));
                    }
                });
            } else {
                // If still no cover, use placeholder
                javafx.application.Platform
                        .runLater(() -> coverImageView.setImage(new Image(placeholderUrl, true)));
            }
        } catch (Exception e) {
            System.err.println("Error getting direct cover: " + e.getMessage());
            javafx.application.Platform
                    .runLater(() -> coverImageView.setImage(new Image(placeholderUrl, true)));
        }
    }
}