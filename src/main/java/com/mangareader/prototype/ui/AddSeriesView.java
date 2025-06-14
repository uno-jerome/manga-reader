package com.mangareader.prototype.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.mangareader.prototype.model.Manga;
import com.mangareader.prototype.model.SearchParams;
import com.mangareader.prototype.model.SearchResult;
import com.mangareader.prototype.source.MangaSource;
import com.mangareader.prototype.source.impl.MangaDexSource;
import com.mangareader.prototype.util.ImageCache;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;

public class AddSeriesView extends VBox implements ThemeManager.ThemeChangeListener {
    private final ComboBox<MangaSource> sourceSelector;
    private final TextField searchField;
    private final Button searchButton;
    private final CheckBox nsfwCheckbox;
    private final GridPane mangaGrid;
    private final List<MangaSource> sources;
    private final ScrollPane scrollPane;
    private final ThemeManager themeManager;
    private int columns = 5;
    private List<Manga> currentResults = new ArrayList<>();
    private final int CARD_WIDTH = 180;
    private final int CARD_HEIGHT = 270;
    private final int MIN_COLUMNS = 5;
    private final int MAX_COLUMNS = 6;
    private final Map<String, VBox> mangaNodeCache = new HashMap<>();
    private Consumer<Manga> onMangaSelectedCallback;

    // Pagination components
    private Pagination pagination;
    private Label resultsCountLabel;
    private int currentPage = 1;
    private int totalPages = 1;
    private int itemsPerPage = 20;

    // Advanced search components
    private Button advancedSearchButton;
    private VBox advancedSearchPane;
    private FlowPane genreSelector;
    private ComboBox<String> statusSelector;
    private final SearchParams searchParams = new SearchParams();
    private Map<String, CheckBox> genreCheckboxes = new HashMap<>();
    private boolean isAdvancedSearchVisible = false;

    public AddSeriesView() {
        this(null);
    }

    public AddSeriesView(Consumer<Manga> onMangaSelectedCallback) {
        this.onMangaSelectedCallback = onMangaSelectedCallback;
        this.themeManager = ThemeManager.getInstance();

        setSpacing(16);
        setPadding(new Insets(24));
        setAlignment(Pos.TOP_CENTER);

        // Initialize sources e.g., MangaDexSource
        sources = new ArrayList<>();
        sources.add(new MangaDexSource());

        // Source selector
        sourceSelector = new ComboBox<>();
        sourceSelector.getItems().addAll(sources);
        sourceSelector.setPromptText("Select Source");
        sourceSelector.setMaxWidth(200);
        sourceSelector.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(MangaSource source, boolean empty) {
                super.updateItem(source, empty);
                setText((empty || source == null) ? null : source.getName());
            }
        });
        sourceSelector.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(MangaSource source, boolean empty) {
                super.updateItem(source, empty);
                setText((empty || source == null) ? "Select Source" : source.getName());
            }
        });
        if (!sources.isEmpty())
            sourceSelector.getSelectionModel().selectFirst();

        // Source selection listener to update genre/status filters if available
        sourceSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateFiltersIfAvailable(newVal);
            }
        });

        // Search bar
        searchField = new TextField();
        searchField.setPromptText("Search for a series...");
        searchField.setPrefWidth(300);
        searchButton = new Button("Search");

        // Advanced search button
        advancedSearchButton = new Button("Filters");
        advancedSearchButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        advancedSearchButton.setOnAction(e -> toggleAdvancedSearch());

        // NSFW Checkbox
        nsfwCheckbox = new CheckBox("NSFW");
        nsfwCheckbox.setSelected(false);
        nsfwCheckbox.setTooltip(new Tooltip("Show NSFW content"));

        // Initial style (unchecked state): white box with gray border, no checkmark
        nsfwCheckbox.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: #666;" +
                        "-fx-background-color: transparent;" + // Overall control background
                        "-fx-box-fill: white;" + // White background for the checkbox box
                        "-fx-box-border: #ccc;" + // Gray border for the checkbox box
                        "-fx-border-width: 1px;" + // Border width for the box
                        "-fx-mark-color: transparent;" // No checkmark visible when unchecked
        );

        // Add listener to update style when checked/unchecked
        nsfwCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Checked state: blue box with white checkmark
                nsfwCheckbox.setStyle(
                        "-fx-font-size: 14px;" +
                                "-fx-text-fill: #666;" + // Text color remains gray
                                "-fx-background-color: transparent;" +
                                "-fx-mark-color: white;" + // White checkmark
                                "-fx-box-fill: #007bff;" + // Blue background for the checkbox box
                                "-fx-box-border: #007bff;" + // Blue border for the checkbox box
                                "-fx-border-width: 1px;");
            } else {
                // Unchecked state: white box with gray border, no checkmark
                nsfwCheckbox.setStyle(
                        "-fx-font-size: 14px;" +
                                "-fx-text-fill: #666;" + // Text color remains gray
                                "-fx-background-color: transparent;" +
                                "-fx-mark-color: transparent;" + // Make checkmark transparent when unchecked
                                "-fx-box-fill: white;" + // White background for the checkbox box
                                "-fx-box-border: #ccc;" + // Gray border for the checkbox box
                                "-fx-border-width: 1px;");
            }

            // Update search params
            searchParams.setIncludeNsfw(newVal);
        });

        // Setup search box
        HBox searchBox = new HBox(8, sourceSelector, searchField, searchButton, advancedSearchButton, nsfwCheckbox);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        // Create advanced search pane
        setupAdvancedSearchPane();

        // Create pagination controls
        resultsCountLabel = new Label("No results");
        resultsCountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        pagination = new Pagination();
        pagination.setPageCount(1);
        pagination.setCurrentPageIndex(0);
        pagination.setMaxPageIndicatorCount(10);
        pagination.setStyle("-fx-border-color: transparent;");

        // Pagination change listener
        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (oldVal.intValue() != newVal.intValue()) {
                currentPage = newVal.intValue() + 1; // Convert from 0-based to 1-based
                searchParams.setPage(currentPage);
                performAdvancedSearch();
            }
        });

        HBox paginationBox = new HBox(20, resultsCountLabel, pagination);
        paginationBox.setAlignment(Pos.CENTER_LEFT);
        paginationBox.setPadding(new Insets(10, 0, 10, 0));

        // Manga grid
        mangaGrid = new GridPane();
        mangaGrid.setHgap(16);
        mangaGrid.setVgap(16);
        mangaGrid.setPadding(new Insets(16, 0, 0, 0));

        // Wrap grid in a scroll pane
        scrollPane = new ScrollPane(mangaGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #181818;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Responsive columns
        widthProperty().addListener((obs, oldVal, newVal) -> updateGridColumns());
        scrollPane.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> updateGridColumns());
        updateGridColumns();

        updateMangaGridWithPlaceholders();

        // Search button action
        searchButton.setOnAction(e -> {
            String query = searchField.getText().trim();
            searchParams.setQuery(query);
            searchParams.setPage(1);
            searchParams.setLimit(itemsPerPage);
            currentPage = 1;
            pagination.setCurrentPageIndex(0);
            performAdvancedSearch();
        });

        // Add Enter key support for search field
        searchField.setOnAction(e -> {
            String query = searchField.getText().trim();
            searchParams.setQuery(query);
            searchParams.setPage(1);
            searchParams.setLimit(itemsPerPage);
            currentPage = 1;
            pagination.setCurrentPageIndex(0);
            performAdvancedSearch();
        });

        // Add all components to main layout
        VBox contentBox = new VBox(10,
                searchBox,
                advancedSearchPane, // Initially hidden, will toggle with advancedSearchButton
                paginationBox,
                scrollPane);

        getChildren().add(contentBox);

        // Hide advanced search panel initially
        advancedSearchPane.setVisible(false);
        advancedSearchPane.setManaged(false);

        // Fetch and display default manga list
        fetchAndDisplayDefaultManga();

        // Register theme listener after initialization
        themeManager.addThemeChangeListener(this);
    }

    private void setupAdvancedSearchPane() {
        advancedSearchPane = new VBox(15);
        advancedSearchPane.setPadding(new Insets(15));
        advancedSearchPane.setStyle(
                "-fx-background-color: #f8f9fa; " +
                        "-fx-border-color: #dee2e6; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5;");

        // Genre section
        Label genreLabel = new Label("Genres");
        genreLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        genreSelector = new FlowPane();
        genreSelector.setHgap(10);
        genreSelector.setVgap(8);
        genreSelector.setPrefWrapLength(800);

        // Status section
        Label statusLabel = new Label("Status");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        statusSelector = new ComboBox<>();
        statusSelector.setPromptText("Any Status");
        statusSelector.setPrefWidth(200);

        // Status change listener
        statusSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals("Any Status")) {
                searchParams.setStatus(newVal.toLowerCase());
            } else {
                searchParams.setStatus(null);
            }
        });

        // Filter buttons
        Button applyFiltersButton = new Button("Apply Filters");
        applyFiltersButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        applyFiltersButton.setOnAction(e -> {
            searchParams.setPage(1);
            currentPage = 1;
            pagination.setCurrentPageIndex(0);
            performAdvancedSearch();
        });

        Button resetFiltersButton = new Button("Reset Filters");
        resetFiltersButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        resetFiltersButton.setOnAction(e -> resetFilters());

        HBox buttonBox = new HBox(10, applyFiltersButton, resetFiltersButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Assemble the advanced search pane
        advancedSearchPane.getChildren().addAll(
                genreLabel,
                genreSelector,
                new Separator(),
                statusLabel,
                statusSelector,
                new Separator(),
                buttonBox);

        // Initialize filters if a source is selected
        if (sourceSelector.getValue() != null) {
            updateFiltersIfAvailable(sourceSelector.getValue());
        }
    }

    private void updateFiltersIfAvailable(MangaSource source) {
        try {
            updateGenreFilters(source);
            updateStatusFilters(source);
        } catch (Exception e) {
            System.err.println("Error updating filters: " + e.getMessage());
            // Handle any exceptions that might occur if the source doesn't support filters
        }
    }

    private void updateGenreFilters(MangaSource source) {
        genreSelector.getChildren().clear();
        genreCheckboxes.clear();

        List<String> genres = source.getAvailableGenres();
        for (String genre : genres) {
            CheckBox genreCheck = new CheckBox(genre);
            genreCheck.setStyle("-fx-padding: 5px;");
            genreCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    searchParams.addIncludedGenre(genre);
                } else {
                    searchParams.removeIncludedGenre(genre);
                }
            });
            genreSelector.getChildren().add(genreCheck);
            genreCheckboxes.put(genre, genreCheck);
        }
    }

    private void updateStatusFilters(MangaSource source) {
        statusSelector.getItems().clear();
        statusSelector.getItems().add("Any Status");
        statusSelector.getItems().addAll(source.getAvailableStatuses());
        statusSelector.setValue("Any Status");
    }

    private void toggleAdvancedSearch() {
        isAdvancedSearchVisible = !isAdvancedSearchVisible;
        advancedSearchPane.setVisible(isAdvancedSearchVisible);
        advancedSearchPane.setManaged(isAdvancedSearchVisible);

        // Update button style
        if (isAdvancedSearchVisible) {
            advancedSearchButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        } else {
            advancedSearchButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        }
    }

    private void resetFilters() {
        // Clear genre checkboxes
        for (CheckBox checkbox : genreCheckboxes.values()) {
            checkbox.setSelected(false);
        }

        // Reset status
        statusSelector.setValue("Any Status");

        // Clear search params
        searchParams.clearParams();

        // Maintain current search query and NSFW setting
        searchParams.setQuery(searchField.getText().trim());
        searchParams.setIncludeNsfw(nsfwCheckbox.isSelected());
        searchParams.setPage(1);
        searchParams.setLimit(itemsPerPage);

        // Reset pagination
        currentPage = 1;
        pagination.setCurrentPageIndex(0);
    }

    private void fetchAndDisplayDefaultManga() {
        searchParams.setQuery("");
        searchParams.setPage(1);
        searchParams.setLimit(itemsPerPage);
        searchParams.setIncludeNsfw(nsfwCheckbox.isSelected());

        // Use the basic search method for the initial load to ensure compatibility
        fetchAndDisplayManga("");
    }

    private void updateGridColumns() {
        double availableWidth = scrollPane.getViewportBounds().getWidth();
        int newColumns = Math.max(MIN_COLUMNS, (int) (availableWidth / (CARD_WIDTH + mangaGrid.getHgap())));
        newColumns = Math.min(newColumns, MAX_COLUMNS);
        if (newColumns != columns) {
            columns = newColumns;
            updateMangaGridWithResults(currentResults);
        }
    }

    private void updateMangaGridWithPlaceholders() {
        mangaGrid.getChildren().clear();

        // Create loading indicator
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(60, 60);
        Label loadingLabel = new Label("Loading manga...");
        loadingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");

        VBox loadingBox = new VBox(10, progressIndicator, loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);

        mangaGrid.add(loadingBox, 0, 0, columns, 1);
    }

    private void fetchAndDisplayManga(String query) {
        MangaSource selectedSource = sourceSelector.getValue();
        if (selectedSource == null)
            return;

        // Create SearchParams for advanced search
        SearchParams params = new SearchParams();
        params.setQuery(query);
        params.setIncludeNsfw(nsfwCheckbox.isSelected());
        params.setPage(1);
        params.setLimit(itemsPerPage);

        // Show loading indicator
        updateMangaGridWithPlaceholders();

        // Run in background thread
        new Thread(() -> {
            try {
                // Try to use advanced search first
                SearchResult result = selectedSource.advancedSearch(params);
                Platform.runLater(() -> {
                    // Update pagination
                    currentPage = result.getCurrentPage();
                    totalPages = result.getTotalPages();
                    pagination.setPageCount(totalPages);
                    pagination.setCurrentPageIndex(currentPage - 1); // Convert from 1-based to 0-based
                    resultsCountLabel.setText(String.format("Found %d results", result.getTotalResults()));

                    // Update grid
                    updateMangaGridWithResults(result.getResults());
                });
            } catch (Exception e) {
                // Fall back to basic search if advanced search fails
                System.err.println("Advanced search failed, falling back to basic search: " + e.getMessage());
                List<Manga> results = selectedSource.search(query, nsfwCheckbox.isSelected());
                Platform.runLater(() -> {
                    resultsCountLabel.setText(String.format("Found %d results", results.size()));
                    pagination.setPageCount(1);
                    pagination.setCurrentPageIndex(0);
                    updateMangaGridWithResults(results);
                });
            }
        }).start();
    }

    private void performAdvancedSearch() {
        MangaSource selectedSource = sourceSelector.getValue();
        if (selectedSource == null)
            return;

        // Show loading indicator
        updateMangaGridWithPlaceholders();

        // Run in background thread
        new Thread(() -> {
            try {
                SearchResult result = selectedSource.advancedSearch(searchParams);
                Platform.runLater(() -> {
                    // Update pagination
                    currentPage = result.getCurrentPage();
                    totalPages = result.getTotalPages();
                    pagination.setPageCount(totalPages);
                    pagination.setCurrentPageIndex(currentPage - 1); // Convert from 1-based to 0-based
                    resultsCountLabel.setText(String.format("Found %d results", result.getTotalResults()));

                    // Update grid
                    updateMangaGridWithResults(result.getResults());
                });
            } catch (Exception e) {
                // Fall back to basic search if advanced search fails
                System.err.println("Advanced search failed, falling back to basic search: " + e.getMessage());
                List<Manga> results = selectedSource.search(searchParams.getQuery(), searchParams.isIncludeNsfw());
                Platform.runLater(() -> {
                    resultsCountLabel.setText(String.format("Found %d results", results.size()));
                    pagination.setPageCount(1);
                    pagination.setCurrentPageIndex(0);
                    updateMangaGridWithResults(results);
                });
            }
        }).start();
    }

    private void updateMangaGridWithResults(List<Manga> mangaList) {
        currentResults = mangaList;
        mangaGrid.getChildren().clear();

        if (mangaList.isEmpty()) {
            Label noResultsLabel = new Label("No results found");
            noResultsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
            mangaGrid.add(noResultsLabel, 0, 0);
            return;
        }

        for (int i = 0; i < mangaList.size(); i++) {
            Manga manga = mangaList.get(i);
            VBox coverBox = mangaNodeCache.computeIfAbsent(manga.getId(), id -> createMangaCover(manga));
            mangaGrid.add(coverBox, i % columns, i / columns);
        }
    }

    private VBox createMangaCover(Manga manga) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.TOP_CENTER);
        box.setPrefWidth(CARD_WIDTH);
        box.setPrefHeight(CARD_HEIGHT + 40);
        box.setPadding(new Insets(0, 0, 8, 0));
        box.setStyle(
                "-fx-background-color: #222;" +
                        "-fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, #000, 4, 0, 0, 2);");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(CARD_WIDTH);
        imageView.setFitHeight(CARD_HEIGHT);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        imageView.setCache(true);

        StackPane imageContainer = new StackPane(imageView);
        imageContainer.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        Rectangle clip = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imageContainer.setClip(clip);

        System.out.println("Loading cover: " + manga.getCoverUrl());
        try {
            ImageCache imageCache = ImageCache.getInstance();
            if (manga.getCoverUrl() != null && !manga.getCoverUrl().isEmpty()) {
                Image image = imageCache.getImage(manga.getCoverUrl());
                imageView.setImage(image);
            } else {
                Image placeholderImage = imageCache.getPlaceholderImage("No+Cover");
                imageView.setImage(placeholderImage);
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + manga.getCoverUrl() + " | " + e.getMessage());
            ImageCache imageCache = ImageCache.getInstance();
            Image errorImage = imageCache.getPlaceholderImage("Error");
            imageView.setImage(errorImage);
        }

        Label titleLabel = new Label(manga.getTitle());
        titleLabel.setWrapText(false);
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setMaxWidth(CARD_WIDTH - (8 * 2)); // Title text constrained by its HBox padding
        titleLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #eee; -fx-font-weight: bold;");

        HBox titleBox = new HBox(titleLabel);
        titleBox.setPrefWidth(CARD_WIDTH);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(0, 8, 0, 8));

        box.getChildren().addAll(imageContainer, titleBox);
        VBox.setVgrow(titleBox, Priority.NEVER);
        VBox.setMargin(titleBox, new Insets(5, 0, 0, 0));

        // Add click handler to the VBox
        box.setOnMouseClicked(event -> {
            MangaSource selectedSource = sourceSelector.getValue();
            if (selectedSource != null) {
                // Fetch full manga details before showing modal
                new Thread(() -> {
                    selectedSource.getMangaDetails(manga.getId()).ifPresentOrElse(
                            fullManga -> Platform.runLater(() -> {
                                AddSeriesModal modal = new AddSeriesModal(fullManga);
                                Optional<Manga> result = modal.showAndAwaitResult();
                                // If user confirms, notify callback
                                if (onMangaSelectedCallback != null && result.isPresent()) {
                                    onMangaSelectedCallback.accept(result.get());
                                }
                            }),
                            () -> Platform.runLater(() -> {
                                AddSeriesModal modal = new AddSeriesModal(manga);
                                Optional<Manga> result = modal.showAndAwaitResult();
                                // If user confirms, notify callback
                                if (onMangaSelectedCallback != null && result.isPresent()) {
                                    onMangaSelectedCallback.accept(result.get());
                                }
                            }));
                }).start();
            } else {
                AddSeriesModal modal = new AddSeriesModal(manga);
                Optional<Manga> result = modal.showAndAwaitResult();
                // If user confirms, notify callback
                if (onMangaSelectedCallback != null && result.isPresent()) {
                    onMangaSelectedCallback.accept(result.get());
                }
            }
        });

        return box;
    }

    @Override
    public void onThemeChanged(ThemeManager.Theme newTheme) {
        // Update the main background
        String backgroundColor = themeManager.getBackgroundColor();
        setStyle("-fx-background-color: " + backgroundColor + ";");

        // Update existing manga card themes without clearing the grid
        updateExistingMangaCardThemes();

        // Update other UI components
        updateComponentThemes();
    }

    private void updateExistingMangaCardThemes() {
        String cardBackgroundColor = themeManager.isDarkTheme() ? "#222" : "#fff";
        String textColor = themeManager.getTextColor();

        // Update existing manga cards in the grid
        mangaGrid.getChildren().forEach(node -> {
            if (node instanceof VBox) {
                VBox card = (VBox) node;
                // Update card background
                String currentStyle = card.getStyle();
                String newStyle = currentStyle.replaceAll("-fx-background-color: [^;]+;", "")
                        + " -fx-background-color: " + cardBackgroundColor + ";";
                card.setStyle(newStyle);

                // Update text color in labels
                card.getChildren().forEach(child -> {
                    if (child instanceof HBox) {
                        HBox titleBox = (HBox) child;
                        titleBox.getChildren().forEach(titleChild -> {
                            if (titleChild instanceof Label) {
                                Label titleLabel = (Label) titleChild;
                                String labelStyle = titleLabel.getStyle();
                                String newLabelStyle = labelStyle.replaceAll("-fx-text-fill: [^;]+;", "")
                                        + " -fx-text-fill: " + textColor + ";";
                                titleLabel.setStyle(newLabelStyle);
                            }
                        });
                    }
                });
            }
        });
    }

    private void updateComponentThemes() {
        String backgroundColor = themeManager.getBackgroundColor();
        String textColor = themeManager.getTextColor();
        String secondaryBackgroundColor = themeManager.getSecondaryBackgroundColor();
        String borderColor = themeManager.getBorderColor();

        // Update search field
        if (searchField != null) {
            searchField.setStyle(String.format(
                    "-fx-background-color: %s; " +
                            "-fx-text-fill: %s; " +
                            "-fx-border-color: %s; " +
                            "-fx-border-width: 1px; " +
                            "-fx-background-radius: 4px; " +
                            "-fx-border-radius: 4px;",
                    secondaryBackgroundColor, textColor, borderColor));
        }

        // Update advanced search pane
        if (advancedSearchPane != null) {
            advancedSearchPane.setStyle(String.format(
                    "-fx-background-color: %s; " +
                            "-fx-border-color: %s; " +
                            "-fx-border-radius: 5; " +
                            "-fx-background-radius: 5;",
                    secondaryBackgroundColor, borderColor));
        }

        // Update scroll pane
        if (scrollPane != null) {
            scrollPane.setStyle(String.format(
                    "-fx-background-color: %s; " +
                            "-fx-border-color: %s;",
                    secondaryBackgroundColor, borderColor));
        }
    }
}
