package com.mangareader.prototype.ui;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.mangareader.prototype.model.Chapter;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ChapterListView extends VBox {
    private final TableView<Chapter> chapterTable;
    private final Label statusLabel;
    private final Button refreshButton;
    private final Button downloadAllButton;

    public ChapterListView() {
        setPadding(new Insets(20));
        setSpacing(10);

        // header
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label titleLabel = new Label("Chapters");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        refreshButton = new Button("Refresh");
        downloadAllButton = new Button("Download All");

        header.getChildren().addAll(titleLabel, refreshButton, downloadAllButton);

        // Table
        chapterTable = createChapterTable();

        // Status label
        statusLabel = new Label("No chapters available");
        statusLabel.setAlignment(javafx.geometry.Pos.CENTER);

        getChildren().addAll(header, chapterTable, statusLabel);

        VBox.setVgrow(chapterTable, Priority.ALWAYS);
    }

    private TableView<Chapter> createChapterTable() {
        TableView<Chapter> table = new TableView<>();

        // Chapter number column
        TableColumn<Chapter, Double> numberCol = new TableColumn<>("Chapter");
        numberCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getNumber()).asObject());
        numberCol.setPrefWidth(100);

        // Title column
        TableColumn<Chapter, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        titleCol.setPrefWidth(300);

        // Volume column
        TableColumn<Chapter, String> volumeCol = new TableColumn<>("Volume");
        volumeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getVolume()));
        volumeCol.setPrefWidth(100);

        // Release date column
        TableColumn<Chapter, String> dateCol = new TableColumn<>("Release Date");
        dateCol.setCellValueFactory(data -> {
            if (data.getValue().getReleaseDate() != null) {
                return new SimpleStringProperty(
                        data.getValue().getReleaseDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
            return new SimpleStringProperty("");
        });
        dateCol.setPrefWidth(150);

        // Status column
        TableColumn<Chapter, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> {
            String status = data.getValue().isDownloaded() ? "Downloaded" : "Not Downloaded";
            return new SimpleStringProperty(status);
        });
        statusCol.setPrefWidth(120);

        // Actions column
        TableColumn<Chapter, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button readButton = new Button("Read");
            private final Button downloadButton = new Button("Download");
            private final HBox buttons = new HBox(5, readButton, downloadButton);

            {
                readButton.setOnAction(e -> {
                    Chapter chapter = getTableView().getItems().get(getIndex());
                });

                downloadButton.setOnAction(e -> {
                    Chapter chapter = getTableView().getItems().get(getIndex());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
        actionsCol.setPrefWidth(200);

        table.getColumns().addAll(numberCol, titleCol, volumeCol, dateCol, statusCol, actionsCol);

        return table;
    }

    public void displayChapters(List<Chapter> chapters) {
        chapterTable.getItems().clear();

        if (chapters == null || chapters.isEmpty()) {
            statusLabel.setText("No chapters available");
            statusLabel.setVisible(true);
            return;
        }

        chapterTable.getItems().addAll(chapters);
        statusLabel.setVisible(false);
    }

    public TableView<Chapter> getChapterTable() {
        return chapterTable;
    }

    public Button getRefreshButton() {
        return refreshButton;
    }

    public Button getDownloadAllButton() {
        return downloadAllButton;
    }
}