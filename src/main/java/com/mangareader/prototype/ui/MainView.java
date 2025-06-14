package com.mangareader.prototype.ui;

import java.util.List;

import com.mangareader.prototype.model.Chapter;
import com.mangareader.prototype.model.Manga;
import com.mangareader.prototype.service.MangaService;
import com.mangareader.prototype.service.impl.DefaultMangaServiceImpl;

import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * Main view for the manga reader application.
 * Contains the sidebar, content area, and top bar.
 */
public class MainView extends BorderPane {
    private final Sidebar sidebar;
    private final StackPane contentArea;
    private final ToolBar topBar;

    public MainView() {
        // Initialize components
        sidebar = new Sidebar();
        contentArea = new StackPane();
        topBar = createTopBar();

        // Set up the layout
        setLeft(sidebar);
        setCenter(contentArea);
        setTop(topBar);

        // Style the main container
        setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        setPadding(new Insets(0));

        // Show LibraryView by default
        showLibraryView();

        // Listen for navigation selection
        sidebar.getNavigationTree().getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                switch (newVal.getValue()) {
                    case "Library" -> showLibraryView();
                    case "Updates" -> showUpdatesView();
                    case "Settings" -> showSettingsView();
                    case "Add Series" -> showAddSeriesView();
                    // Add more cases as needed
                }
            }
        });
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
        contentArea.getChildren().clear();
        contentArea.getChildren().add(new LibraryView());
    }

    private void showUpdatesView() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(new UpdatesView());
    }

    private void showSettingsView() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(new SettingsView());
    }

    private void showAddSeriesView() {
        contentArea.getChildren().clear();
        // Pass a callback to AddSeriesView to handle manga selection
        contentArea.getChildren().add(new AddSeriesView(this::showMangaDetailView));
    }

    private void showMangaDetailView(Manga manga) {
        contentArea.getChildren().clear();
        MangaDetailView mangaDetailView = new MangaDetailView(
                chapter -> showMangaReaderView(chapter, manga), // Pass manga to reader
                this::showAddSeriesView);
        mangaDetailView.displayManga(manga);
        contentArea.getChildren().add(mangaDetailView);
    }

    private void showMangaReaderView(Chapter chapter, Manga manga) {
        contentArea.getChildren().clear();
        MangaReaderView mangaReaderView = new MangaReaderView(() -> {
            // Go back to the manga detail view
            showMangaDetailView(manga);
        });

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
}