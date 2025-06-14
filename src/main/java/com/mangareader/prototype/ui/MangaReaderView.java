package com.mangareader.prototype.ui;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mangareader.prototype.model.Chapter;
import com.mangareader.prototype.service.MangaService;
import com.mangareader.prototype.service.impl.DefaultMangaServiceImpl;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class MangaReaderView extends BorderPane {
    private final StackPane imageContainer;
    private final ImageView currentImageView;
    private final ProgressIndicator progressIndicator;
    private final Label errorLabel;
    private final Label pageInfoLabel;
    private final Button prevButton;
    private final Button nextButton;
    private final Button prevChapterButton;
    private final Button nextChapterButton;
    private final Button backButton;
    private final Slider zoomSlider;
    private final HBox controlsBox;

    private final MangaService mangaService;
    private final ExecutorService executorService;

    private Chapter currentChapter;
    private List<String> pageUrls;
    private int currentPageIndex = 0;
    private double zoomLevel = 1.0;
    private Runnable onBackCallback;

    public MangaReaderView() {
        this(null);
    }

    public MangaReaderView(Runnable onBackCallback) {
        this.onBackCallback = onBackCallback;
        this.mangaService = new DefaultMangaServiceImpl();
        this.executorService = Executors.newSingleThreadExecutor();

        // Main image container with dark background
        imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-color: #2b2b2b;");

        // Current page image view
        currentImageView = new ImageView();
        currentImageView.setPreserveRatio(true);
        currentImageView.setSmooth(true);
        currentImageView.setCache(true);

        // Progress indicator
        progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(50, 50);
        progressIndicator.setVisible(false);

        // Error label
        errorLabel = new Label("Error loading pages.");
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
        errorLabel.setVisible(false);

        // Add image and overlays to container
        imageContainer.getChildren().addAll(currentImageView, progressIndicator, errorLabel);

        // Page info label
        pageInfoLabel = new Label("Page 0 / 0");
        pageInfoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        // Navigation buttons
        backButton = new Button("⬅ Back to Details");
        backButton.setStyle(
                "-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 12;");
        backButton.setOnAction(e -> {
            if (onBackCallback != null) {
                onBackCallback.run();
            }
        });

        prevButton = new Button("◀ Previous");
        prevButton.setDisable(true);
        prevButton.setOnAction(e -> previousPage());

        nextButton = new Button("Next ▶");
        nextButton.setDisable(true);
        nextButton.setOnAction(e -> nextPage());

        // Chapter navigation buttons
        prevChapterButton = new Button("◄◄ Prev Chapter");
        prevChapterButton.setStyle(
                "-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 10;");
        prevChapterButton.setDisable(true); // Will be enabled when we have chapter list

        nextChapterButton = new Button("Next Chapter ►►");
        nextChapterButton.setStyle(
                "-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 10;");
        nextChapterButton.setDisable(true); // Will be enabled when we have chapter list

        // Zoom slider
        zoomSlider = new Slider(0.1, 3.0, 1.0);
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setPrefWidth(200);
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            zoomLevel = newVal.doubleValue();
            updateImageSize();
        });

        // Auto-fit button for quick fit-to-window
        Button autoFitButton = new Button("Fit");
        autoFitButton.setOnAction(e -> {
            zoomSlider.setValue(1.0);
            updateImageSize();
        });

        Label zoomLabel = new Label("Zoom:");
        zoomLabel.setStyle("-fx-text-fill: white;");

        // Controls container - make it more visible
        controlsBox = new HBox(15);
        controlsBox.setAlignment(Pos.CENTER);
        controlsBox.setPadding(new Insets(15));
        controlsBox.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.8); -fx-border-color: #444; -fx-border-width: 1 0 0 0;");
        controlsBox.getChildren().addAll(
                backButton,
                new Label("   "), // Spacer
                prevChapterButton,
                new Label(" "), // Small spacer
                prevButton, pageInfoLabel, nextButton,
                new Label(" "), // Small spacer
                nextChapterButton,
                new Label("   "), // Spacer
                zoomLabel, zoomSlider, autoFitButton);

        // Layout
        setCenter(imageContainer);
        setBottom(controlsBox);

        // Keyboard event handling
        setFocusTraversable(true);
        setOnKeyPressed(this::handleKeyPressed);

        // Auto-focus for keyboard events
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> requestFocus());
            }
        });

        // Listen for window resize to update image size
        imageContainer.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            Platform.runLater(this::updateImageSize);
        });

        imageContainer.heightProperty().addListener((obs, oldHeight, newHeight) -> {
            Platform.runLater(this::updateImageSize);
        });
    }

    public void loadChapter(Chapter chapter) {
        if (chapter == null) {
            displayError("No chapter provided.");
            return;
        }

        this.currentChapter = chapter;
        this.currentPageIndex = 0;
        progressIndicator.setVisible(true);
        errorLabel.setVisible(false);
        currentImageView.setImage(null);

        executorService.submit(() -> {
            try {
                List<String> urls = mangaService.getChapterPages(chapter.getMangaId(), chapter.getId());
                Platform.runLater(() -> {
                    this.pageUrls = urls;
                    progressIndicator.setVisible(false);
                    if (urls != null && !urls.isEmpty()) {
                        displayCurrentPage();
                        updateNavigationButtons();
                    } else {
                        displayError("No pages found for this chapter.");
                    }
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

    private void displayCurrentPage() {
        if (pageUrls == null || pageUrls.isEmpty() || currentPageIndex < 0 || currentPageIndex >= pageUrls.size()) {
            return;
        }

        String pageUrl = pageUrls.get(currentPageIndex);
        Image image = new Image(pageUrl, true);
        currentImageView.setImage(image);

        // Update page info
        pageInfoLabel.setText(String.format("Page %d / %d", currentPageIndex + 1, pageUrls.size()));

        // Listen for image to load and then resize
        image.progressProperty().addListener((obs, oldProgress, newProgress) -> {
            if (newProgress.doubleValue() >= 1.0) {
                Platform.runLater(this::updateImageSize);
            }
        });

        // Also update size immediately in case image loads quickly
        Platform.runLater(this::updateImageSize);

        // Ensure focus for keyboard navigation
        Platform.runLater(() -> requestFocus());
    }

    private void updateImageSize() {
        if (currentImageView.getImage() == null)
            return;

        // Get available space (minus controls)
        double availableWidth = imageContainer.getWidth();
        double availableHeight = imageContainer.getHeight();

        if (availableWidth <= 0 || availableHeight <= 0) {
            // Fallback to reasonable defaults based on scene size
            availableWidth = getScene() != null ? getScene().getWidth() - 40 : 800;
            availableHeight = getScene() != null ? getScene().getHeight() - 100 : 600;
        }

        // Account for padding and controls
        availableWidth -= 40; // Account for padding
        availableHeight -= 80; // Account for controls and padding

        // Get image dimensions
        Image image = currentImageView.getImage();
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();

        if (imageWidth <= 0 || imageHeight <= 0)
            return;

        // Calculate scale factor to fit image in available space
        double scaleX = availableWidth / imageWidth;
        double scaleY = availableHeight / imageHeight;
        double scale = Math.min(scaleX, scaleY) * zoomLevel;

        // Ensure minimum readable size
        scale = Math.max(scale, 0.1);

        // Set the size
        currentImageView.setFitWidth(imageWidth * scale);
        currentImageView.setFitHeight(imageHeight * scale);
    }

    private void previousPage() {
        if (currentPageIndex > 0) {
            currentPageIndex--;
            displayCurrentPage();
            updateNavigationButtons();
        }
    }

    private void nextPage() {
        if (pageUrls != null && currentPageIndex < pageUrls.size() - 1) {
            currentPageIndex++;
            displayCurrentPage();
            updateNavigationButtons();
        }
    }

    private void updateNavigationButtons() {
        prevButton.setDisable(currentPageIndex <= 0);
        nextButton.setDisable(pageUrls == null || currentPageIndex >= pageUrls.size() - 1);
    }

    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();

        if (code == KeyCode.LEFT || code == KeyCode.A) {
            previousPage();
            event.consume();
        } else if (code == KeyCode.RIGHT || code == KeyCode.D) {
            nextPage();
            event.consume();
        } else if (code == KeyCode.PLUS || code == KeyCode.EQUALS) {
            zoomSlider.setValue(Math.min(3.0, zoomSlider.getValue() + 0.1));
            event.consume();
        } else if (code == KeyCode.MINUS) {
            zoomSlider.setValue(Math.max(0.1, zoomSlider.getValue() - 0.1));
            event.consume();
        } else if (code == KeyCode.DIGIT0) {
            zoomSlider.setValue(1.0); // Reset zoom
            event.consume();
        } else if (code == KeyCode.ESCAPE) {
            if (onBackCallback != null) {
                onBackCallback.run();
            }
            event.consume();
        }
    }

    private void displayError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        currentImageView.setImage(null);
        pageInfoLabel.setText("Page 0 / 0");
        updateNavigationButtons();
    }

    // Getter methods for accessing current state
    public Chapter getCurrentChapter() {
        return currentChapter;
    }

    public int getCurrentPageIndex() {
        return currentPageIndex;
    }

    public int getTotalPages() {
        return pageUrls != null ? pageUrls.size() : 0;
    }
}