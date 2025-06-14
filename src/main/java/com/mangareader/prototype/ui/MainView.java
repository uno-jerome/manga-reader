package com.mangareader.prototype.ui;

import java.util.List;

import com.mangareader.prototype.model.Chapter;
import com.mangareader.prototype.model.Manga;
import com.mangareader.prototype.service.LibraryService;
import com.mangareader.prototype.service.MangaService;
import com.mangareader.prototype.service.impl.DefaultMangaServiceImpl;
import com.mangareader.prototype.service.impl.LibraryServiceImpl;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * Main view for the manga reader application.
 * Contains the sidebar, content area, and top bar.
 */
public class MainView extends BorderPane implements ThemeManager.ThemeChangeListener {
    private final Sidebar sidebar;
    private final StackPane contentArea;
    private final ToolBar topBar;
    private final ThemeManager themeManager;
    private LibraryView currentLibraryView; // Track current library view for refreshing
    private boolean programmaticSelection = false; // Flag to prevent listener loops

    public MainView() {
        // Initialize theme manager
        themeManager = ThemeManager.getInstance();

        // Initialize components
        sidebar = new Sidebar();
        contentArea = new StackPane();
        topBar = createTopBar();

        // Set up the layout
        setLeft(sidebar);
        setCenter(contentArea);
        setTop(topBar);

        // Style the main container using theme colors
        String backgroundColor = themeManager.getBackgroundColor();
        setBackground(new Background(new BackgroundFill(Color.web(backgroundColor), null, null)));
        setPadding(new Insets(0));

        // Apply initial theme to toolbar
        updateToolbarTheme();

        // Show LibraryView by default
        showLibraryView();

        // Listen for navigation selection
        sidebar.getNavigationTree().getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !programmaticSelection) {
                switch (newVal.getValue()) {
                    case "Library" -> showLibraryView();
                    case "Updates" -> showUpdatesView();
                    case "Settings" -> showSettingsView();
                    case "Add Series" -> showAddSeriesView(); // Direct navigation to AddSeriesView for browsing new
                                                              // manga
                    // Add more cases as needed
                }
            }
        });

        // Register theme listener after all initialization is complete
        themeManager.addThemeChangeListener(this);
    }

    private ToolBar createTopBar() {
        ToolBar toolBar = new ToolBar();

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search manga...");
        searchField.setPrefWidth(300);

        toolBar.getItems().add(searchField);

        return toolBar;
    }

    private void showLibraryView() {
        // Update sidebar selection to "Library"
        updateSidebarSelection("Library");

        contentArea.getChildren().clear();
        currentLibraryView = new LibraryView(this::showMangaDetailView);
        // Set up the "Add New Series" button to navigate to AddSeriesView
        currentLibraryView.setOnAddSeriesCallback(this::showAddSeriesView);
        contentArea.getChildren().add(currentLibraryView);
    }

    private void showUpdatesView() {
        // Update sidebar selection to "Updates"
        updateSidebarSelection("Updates");

        contentArea.getChildren().clear();
        contentArea.getChildren().add(new UpdatesView());
    }

    private void showSettingsView() {
        // Update sidebar selection to "Settings"
        updateSidebarSelection("Settings");

        contentArea.getChildren().clear();
        contentArea.getChildren().add(new SettingsView());
    }

    private void showAddSeriesView() {
        // Update sidebar selection to "Add Series"
        updateSidebarSelection("Add Series");

        contentArea.getChildren().clear();
        // Pass a callback to AddSeriesView to handle manga selection
        contentArea.getChildren().add(new AddSeriesView(this::showMangaDetailView));
    }

    private void showMangaDetailView(Manga manga) {
        contentArea.getChildren().clear();
        MangaDetailView mangaDetailView = new MangaDetailView(
                chapter -> showMangaReaderView(chapter, manga), // Pass manga to reader
                this::showAddSeriesView);

        // Set up "Add to Library" button action
        mangaDetailView.getAddToLibraryButton().setOnAction(e -> {
            LibraryService libraryService = new LibraryServiceImpl();

            // Check if already in library
            if (libraryService.isInLibrary(manga.getId())) {
                mangaDetailView.getAddToLibraryButton().setText("Already in Library");
                mangaDetailView.getAddToLibraryButton().setStyle(
                        "-fx-font-size: 14px; " +
                                "-fx-background-color: #ffc107; " +
                                "-fx-text-fill: black; " +
                                "-fx-padding: 10 20; " +
                                "-fx-background-radius: 5;");
                return;
            }

            // Add to library
            boolean success = libraryService.addToLibrary(manga);

            if (success) {
                // Refresh library if currently showing
                if (currentLibraryView != null) {
                    currentLibraryView.refreshLibrary();
                }

                // Show confirmation message
                mangaDetailView.getAddToLibraryButton().setText("Added to Library!");
                mangaDetailView.getAddToLibraryButton().setDisable(true);
                mangaDetailView.getAddToLibraryButton().setStyle(
                        "-fx-font-size: 14px; " +
                                "-fx-background-color: #28a745; " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 10 20; " +
                                "-fx-background-radius: 5;");

                // Reset after 2 seconds
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        Platform.runLater(() -> {
                            mangaDetailView.getAddToLibraryButton().setText("In Library");
                            mangaDetailView.getAddToLibraryButton().setDisable(false);
                            mangaDetailView.getAddToLibraryButton().setStyle(
                                    "-fx-font-size: 14px; " +
                                            "-fx-background-color: #28a745; " +
                                            "-fx-text-fill: white; " +
                                            "-fx-padding: 10 20; " +
                                            "-fx-background-radius: 5;");
                        });
                    } catch (InterruptedException ex) {
                        System.err.println("Thread interrupted while updating button: " + ex.getMessage());
                        Thread.currentThread().interrupt(); // Restore interrupted status
                    }
                }).start();
            } else {
                // Show error message
                mangaDetailView.getAddToLibraryButton().setText("Failed to Add");
                mangaDetailView.getAddToLibraryButton().setStyle(
                        "-fx-font-size: 14px; " +
                                "-fx-background-color: #dc3545; " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 10 20; " +
                                "-fx-background-radius: 5;");
            }
        });

        mangaDetailView.displayManga(manga);
        contentArea.getChildren().add(mangaDetailView);
    }

    private void showMangaReaderView(Chapter chapter, Manga manga) {
        contentArea.getChildren().clear();
        MangaReaderView mangaReaderView = new MangaReaderView(() -> {
            // Go back to the manga detail view
            showMangaDetailView(manga);
        });

        // Set manga ID for progress tracking
        mangaReaderView.setMangaId(manga.getId());

        // Get all chapters for navigation
        MangaService mangaService = new DefaultMangaServiceImpl();
        List<Chapter> allChapters = mangaService.getChapters(manga.getId());

        // Set chapter list for navigation
        mangaReaderView.setChapterList(allChapters, chapter);

        mangaReaderView.loadChapter(chapter);
        contentArea.getChildren().add(mangaReaderView);
    }

    public Sidebar getSidebar() {
        return sidebar;
    }

    public StackPane getContentArea() {
        return contentArea;
    }

    public ToolBar getTopBar() {
        return topBar;
    }

    private void updateSidebarSelection(String itemName) {
        // Set flag to prevent listener from triggering
        programmaticSelection = true;

        // Find and select the appropriate tree item
        TreeView<String> navigationTree = sidebar.getNavigationTree();
        TreeItem<String> root = navigationTree.getRoot();

        for (TreeItem<String> item : root.getChildren()) {
            if (item.getValue().equals(itemName)) {
                navigationTree.getSelectionModel().select(item);
                break;
            }
        }

        // Reset flag
        programmaticSelection = false;
    }

    @Override
    public void onThemeChanged(ThemeManager.Theme newTheme) {
        // Update the main view background
        String backgroundColor = themeManager.getBackgroundColor();
        setBackground(new Background(new BackgroundFill(Color.web(backgroundColor), null, null)));

        // Update toolbar styling
        if (topBar != null) {
            updateToolbarTheme();
        }
    }

    private void updateToolbarTheme() {
        String backgroundColor = themeManager.getSecondaryBackgroundColor();
        String borderColor = themeManager.getBorderColor();

        topBar.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-border-color: %s; " +
                        "-fx-border-width: 0 0 1 0;",
                backgroundColor, borderColor));
    }
}