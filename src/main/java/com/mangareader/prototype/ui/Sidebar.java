package com.mangareader.prototype.ui;

import java.util.ArrayList;
import java.util.List;

import com.mangareader.prototype.source.MangaSource;
import com.mangareader.prototype.source.impl.MangaDexSource;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class Sidebar extends VBox implements ThemeManager.ThemeChangeListener {
    private final ListView<String> libraryList;
    private final TreeView<String> navigationTree;
    private final List<MangaSource> sources;
    private final ThemeManager themeManager;

    public Sidebar() {
        // Initialize theme manager
        themeManager = ThemeManager.getInstance();

        // Set up the sidebar layout with consistent spacing
        setPrefWidth(250);
        setMinWidth(200);
        setMaxWidth(300);

        // Set consistent spacing before theme application
        setSpacing(15); // Increased for better visual balance
        setPadding(new Insets(15)); // Consistent padding for both themes

        // Initialize sources
        sources = new ArrayList<>();
        sources.add(new MangaDexSource());

        // Create navigation tree
        navigationTree = createNavigationTree();

        // Create library list
        libraryList = createLibraryList();

        // Add components to sidebar
        getChildren().addAll(
                createHeader(),
                new Separator(),
                navigationTree);

        VBox.setVgrow(navigationTree, Priority.ALWAYS);

        // Apply CSS style class for consistent theming
        getStyleClass().add("sidebar");

        // Apply initial theme after all components are created
        updateSidebarTheme();

        // Register theme listener after initialization is complete
        themeManager.addThemeChangeListener(this);
    }

    private Label createHeader() {
        Label header = new Label("MangaReader");
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        return header;
    }

    private TreeView<String> createNavigationTree() {
        TreeItem<String> root = new TreeItem<>("Navigation");

        // Add navigation items
        root.getChildren().addAll(
                new TreeItem<>("Library"),
                new TreeItem<>("Add Series"),
                new TreeItem<>("Settings"));

        TreeView<String> tree = new TreeView<>(root);
        tree.setShowRoot(false);
        return tree;
    }

    private ListView<String> createLibraryList() {
        ListView<String> list = new ListView<>();
        list.setPlaceholder(new Label("No manga in library"));
        return list;
    }

    public ListView<String> getLibraryList() {
        return libraryList;
    }

    public TreeView<String> getNavigationTree() {
        return navigationTree;
    }

    @Override
    public void onThemeChanged(ThemeManager.Theme newTheme) {
        updateSidebarTheme();
    }

    private void updateSidebarTheme() {
        String backgroundColor = themeManager.getSecondaryBackgroundColor();
        String borderColor = themeManager.getBorderColor();

        setBackground(new Background(new BackgroundFill(Color.web(backgroundColor), null, null)));
        setBorder(new Border(new BorderStroke(Color.web(borderColor), BorderStrokeStyle.SOLID, null, null)));

        // Apply CSS style class instead of programmatic styling to maintain consistent
        // spacing
        getStyleClass().clear();
        getStyleClass().add("sidebar");

        // Update tree view theme - use CSS classes for consistency
        if (navigationTree != null) {
            navigationTree.getStyleClass().clear();
            navigationTree.getStyleClass().add("tree-view");
        }

        // Update library list theme - use CSS classes for consistency
        if (libraryList != null) {
            libraryList.getStyleClass().clear();
            libraryList.getStyleClass().add("list-view");
        }
    }
}