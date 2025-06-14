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

public class Sidebar extends VBox {
    private final ListView<String> libraryList;
    private final TreeView<String> navigationTree;
    private final List<MangaSource> sources;

    public Sidebar() {
        // Set up the sidebar layout
        setPrefWidth(250);
        setMinWidth(200);
        setMaxWidth(300);
        setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null, null)));
        setPadding(new Insets(10));

        // Initialize sources
        sources = new ArrayList<>();
        sources.add(new MangaDexSource());

        // Create source selector

        // Create navigation tree
        navigationTree = createNavigationTree();

        // Create library list
        libraryList = createLibraryList();

        // Add components to sidebar
        getChildren().addAll(
                createHeader(),
                new Separator(),
                navigationTree,
                new Separator(),
                new Label("Library"),
                libraryList);

        VBox.setVgrow(navigationTree, Priority.ALWAYS);
        VBox.setVgrow(libraryList, Priority.ALWAYS);
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
                new TreeItem<>("Updates"),
                new TreeItem<>("Add Series"),
                new TreeItem<>("Downloads"),
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

}