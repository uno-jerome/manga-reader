package com.mangareader.prototype.ui;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mangareader.prototype.model.Chapter;
import com.mangareader.prototype.service.LibraryService;
import com.mangareader.prototype.service.MangaService;
import com.mangareader.prototype.service.impl.DefaultMangaServiceImpl;
import com.mangareader.prototype.service.impl.LibraryServiceImpl;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MangaReaderView extends BorderPane {
    private final StackPane imageContainer;
    private final ScrollPane webtoonScrollPane;
    private final VBox webtoonContainer;
    private final ImageView currentImageView;
    private final ProgressIndicator progressIndicator;
    private final Label errorLabel;
    private final Label pageInfoLabel;
    private final Button prevButton;
    private final Button nextButton;
    private final Button prevChapterButton;
    private final Button nextChapterButton;
    private final Button backButton;
    private final Button modeToggleButton;
    private final Slider zoomSlider;
    private final HBox controlsBox;

    private final MangaService mangaService;
    private final LibraryService libraryService;
    private final ExecutorService executorService;

    private Chapter currentChapter;
    private String currentMangaId; // Add manga ID tracking for progress saving
    private List<String> pageUrls;
    private int currentPageIndex = 0;
    private double zoomLevel = 1.0;
    private boolean isWebtoonMode = false;
    private Runnable onBackCallback;

    // Chapter navigation data
    private List<Chapter> chapterList;
    private int currentChapterIndex = -1;

    public MangaReaderView() {
        this(null);
    }

    public MangaReaderView(Runnable onBackCallback) {
        this.onBackCallback = onBackCallback;
        this.mangaService = new DefaultMangaServiceImpl();
        this.libraryService = new LibraryServiceImpl();
        this.executorService = Executors.newSingleThreadExecutor();

        // Main image container with dark background (for traditional mode)
        imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-color: #2b2b2b;");

        // Webtoon container setup (for continuous scrolling)
        webtoonContainer = new VBox();
        webtoonContainer.setStyle("-fx-background-color: #2b2b2b;");
        webtoonContainer.setAlignment(Pos.CENTER);
        webtoonContainer.setSpacing(3); // Minimal spacing for webtoon reading

        webtoonScrollPane = new ScrollPane(webtoonContainer);
        webtoonScrollPane.setStyle("-fx-background-color: #2b2b2b;");
        webtoonScrollPane.setFitToWidth(true);
        webtoonScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        webtoonScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        webtoonScrollPane.setPannable(true); // Allow panning/dragging

        // Current page image view (for traditional mode)
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

        // Mode toggle button
        modeToggleButton = new Button("📖 Traditional");
        modeToggleButton.setStyle(
                "-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 12;");
        modeToggleButton.setOnAction(e -> toggleReadingMode());

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
        prevChapterButton.setDisable(true);
        prevChapterButton.setOnAction(e -> previousChapter());

        nextChapterButton = new Button("Next Chapter ►►");
        nextChapterButton.setStyle(
                "-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 10;");
        nextChapterButton.setDisable(true);
        nextChapterButton.setOnAction(e -> nextChapter());

        // Zoom slider
        zoomSlider = new Slider(0.1, 3.0, 1.0);
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setPrefWidth(200);
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            zoomLevel = newVal.doubleValue();
            if (isWebtoonMode) {
                updateWebtoonImageSizes();
            } else {
                updateImageSize();
            }
        });

        // Auto-fit button for quick fit-to-window
        Button autoFitButton = new Button("Fit");
        autoFitButton.setOnAction(e -> {
            zoomSlider.setValue(1.0);
            if (isWebtoonMode) {
                updateWebtoonImageSizes();
            } else {
                updateImageSize();
            }
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
                modeToggleButton,
                new Label(" "), // Small spacer
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

        // Listen for webtoon container resize to update webtoon images
        webtoonScrollPane.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            if (isWebtoonMode) {
                Platform.runLater(this::updateWebtoonImageSizes);
            }
        });
    }

    private void toggleReadingMode() {
        isWebtoonMode = !isWebtoonMode;

        if (isWebtoonMode) {
            // Switch to webtoon mode
            modeToggleButton.setText("📜 Webtoon");
            setCenter(webtoonScrollPane);
            setupWebtoonView();
            // In webtoon mode, hide individual page navigation but keep other controls
            prevButton.setVisible(false);
            nextButton.setVisible(false);
            pageInfoLabel.setVisible(false);
        } else {
            // Switch to traditional mode
            modeToggleButton.setText("📖 Traditional");
            setCenter(imageContainer);
            displayCurrentPage();
            // Show page navigation in traditional mode
            prevButton.setVisible(true);
            nextButton.setVisible(true);
            pageInfoLabel.setVisible(true);
            updateNavigationButtons();
        }
    }

    private void setupWebtoonView() {
        if (pageUrls == null || pageUrls.isEmpty()) {
            return;
        }

        webtoonContainer.getChildren().clear();
        progressIndicator.setVisible(true);

        // Load all images in sequence for continuous scrolling
        executorService.submit(() -> {
            try {
                for (int i = 0; i < pageUrls.size(); i++) {
                    final int pageIndex = i;
                    final String pageUrl = pageUrls.get(i);

                    Platform.runLater(() -> {
                        ImageView pageImageView = new ImageView();
                        pageImageView.setPreserveRatio(true);
                        pageImageView.setSmooth(true);
                        pageImageView.setCache(true);

                        // Set width to fit the container properly for webtoon reading
                        // Use a larger width for better readability
                        double containerWidth = webtoonScrollPane.getWidth();
                        if (containerWidth <= 0) {
                            containerWidth = getScene() != null ? getScene().getWidth() - 250 : 800;
                        }

                        // Set the image to use most of the available width, applying zoom level
                        double baseWidth = Math.max(600, containerWidth * 0.85);
                        double targetWidth = baseWidth * zoomLevel;
                        pageImageView.setFitWidth(targetWidth);

                        Image image = new Image(pageUrl, true);
                        pageImageView.setImage(image);

                        // Add minimal spacing between pages for webtoon reading
                        if (pageIndex > 0) {
                            Label spacer = new Label("");
                            spacer.setStyle("-fx-background-color: #1a1a1a;");
                            spacer.setPrefHeight(5); // Reduced spacing for webtoon
                            webtoonContainer.getChildren().add(spacer);
                        }

                        webtoonContainer.getChildren().add(pageImageView);

                        // Update progress
                        if (pageIndex == pageUrls.size() - 1) {
                            progressIndicator.setVisible(false);
                        }
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    displayError("Failed to load webtoon pages: " + e.getMessage());
                    progressIndicator.setVisible(false);
                });
            }
        });
    }

    public void loadChapter(Chapter chapter) {
        if (chapter == null) {
            displayError("No chapter provided.");
            return;
        }

        this.currentChapter = chapter;
        this.currentPageIndex = 0;

        // Try to restore reading position if in library
        if (currentMangaId != null && libraryService.isInLibrary(currentMangaId)) {
            restoreReadingPosition();
        }

        // Auto-detect reading mode based on chapter format
        if (chapter.getReadingFormat() != null && "webtoon".equals(chapter.getReadingFormat())) {
            if (!isWebtoonMode) {
                toggleReadingMode();
            }
        } else {
            if (isWebtoonMode) {
                toggleReadingMode();
            }
        }

        progressIndicator.setVisible(true);
        errorLabel.setVisible(false);
        currentImageView.setImage(null);

        // Update chapter navigation buttons
        updateChapterNavigationButtons();

        executorService.submit(() -> {
            try {
                List<String> urls = mangaService.getChapterPages(chapter.getMangaId(), chapter.getId());
                Platform.runLater(() -> {
                    this.pageUrls = urls;
                    progressIndicator.setVisible(false);
                    if (urls != null && !urls.isEmpty()) {
                        if (isWebtoonMode) {
                            setupWebtoonView();
                        } else {
                            displayCurrentPage();
                            updateNavigationButtons();
                        }
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

    private void updateWebtoonImageSizes() {
        if (webtoonContainer == null || webtoonContainer.getChildren().isEmpty()) {
            return;
        }

        double containerWidth = webtoonScrollPane.getWidth();
        if (containerWidth <= 0) {
            containerWidth = getScene() != null ? getScene().getWidth() - 250 : 800;
        }

        // Set the image to use most of the available width, applying zoom level
        double baseWidth = Math.max(600, containerWidth * 0.85);
        double targetWidth = baseWidth * zoomLevel;

        // Update all webtoon images
        webtoonContainer.getChildren().forEach(node -> {
            if (node instanceof ImageView) {
                ImageView imageView = (ImageView) node;
                imageView.setFitWidth(targetWidth);
            }
        });
    }

    private void previousPage() {
        if (currentPageIndex > 0) {
            currentPageIndex--;
            displayCurrentPage();
            updateNavigationButtons();
            saveReadingPosition(); // Auto-save progress
        }
    }

    private void nextPage() {
        if (pageUrls != null && currentPageIndex < pageUrls.size() - 1) {
            currentPageIndex++;
            displayCurrentPage();
            updateNavigationButtons();
            saveReadingPosition(); // Auto-save progress
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

    // Chapter navigation methods
    public void setChapterList(List<Chapter> chapters, Chapter currentChapter) {
        this.chapterList = chapters;
        if (chapters != null && currentChapter != null) {
            // Find current chapter index
            for (int i = 0; i < chapters.size(); i++) {
                if (chapters.get(i).getId().equals(currentChapter.getId())) {
                    this.currentChapterIndex = i;
                    break;
                }
            }
        }
        updateChapterNavigationButtons();
    }

    private void updateChapterNavigationButtons() {
        if (chapterList == null || chapterList.isEmpty()) {
            prevChapterButton.setDisable(true);
            nextChapterButton.setDisable(true);
            return;
        }

        prevChapterButton.setDisable(currentChapterIndex <= 0);
        nextChapterButton.setDisable(currentChapterIndex >= chapterList.size() - 1);
    }

    private void previousChapter() {
        if (chapterList != null && currentChapterIndex > 0) {
            Chapter prevChapter = chapterList.get(currentChapterIndex - 1);
            loadChapter(prevChapter);
            currentChapterIndex--;
            updateChapterNavigationButtons();
        }
    }

    private void nextChapter() {
        if (chapterList != null && currentChapterIndex < chapterList.size() - 1) {
            Chapter nextChapter = chapterList.get(currentChapterIndex + 1);
            loadChapter(nextChapter);
            currentChapterIndex++;
            updateChapterNavigationButtons();
        }
    }

    /**
     * Set the manga ID for progress tracking
     */
    public void setMangaId(String mangaId) {
        this.currentMangaId = mangaId;
    }

    /**
     * Save current reading position to library
     */
    private void saveReadingPosition() {
        if (currentMangaId != null && currentChapter != null &&
                libraryService.isInLibrary(currentMangaId)) {

            int totalPages = pageUrls != null ? pageUrls.size() : 0;
            libraryService.updateReadingPosition(
                    currentMangaId,
                    currentChapter.getId(),
                    currentPageIndex,
                    totalPages);

            // Mark chapter as read if we've reached the end
            if (currentPageIndex >= totalPages - 1) {
                libraryService.markChapterAsRead(currentMangaId, currentChapter.getId());
            }
        }
    }

    /**
     * Restore reading position from library
     */
    private void restoreReadingPosition() {
        if (currentMangaId != null && currentChapter != null) {
            Optional<LibraryService.ReadingPosition> position = libraryService.getReadingPosition(currentMangaId);

            if (position.isPresent()) {
                LibraryService.ReadingPosition pos = position.get();
                // Only restore if it's the same chapter
                if (currentChapter.getId().equals(pos.getChapterId())) {
                    currentPageIndex = Math.max(0, pos.getPageNumber());
                    System.out.println("Restored reading position: page " + (currentPageIndex + 1));
                }
            }
        }
    }

    public List<Chapter> getChapterList() {
        return chapterList;
    }

    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}