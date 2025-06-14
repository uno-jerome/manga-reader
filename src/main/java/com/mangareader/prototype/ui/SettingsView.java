package com.mangareader.prototype.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class SettingsView extends StackPane implements ThemeManager.ThemeChangeListener {
    private final ThemeManager themeManager;
    private Button currentThemeButton;

    public SettingsView() {
        themeManager = ThemeManager.getInstance();
        initializeUI();
        // Register theme listener after UI is initialized
        themeManager.addThemeChangeListener(this);
    }

    private void initializeUI() {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setAlignment(Pos.TOP_LEFT);

        // Title
        Label titleLabel = new Label("Settings");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");

        // Theme section
        VBox themeSection = createThemeSection();

        // Add separator
        Separator separator = new Separator();
        separator.setPrefWidth(400);

        // Additional settings can be added here in the future

        mainContainer.getChildren().addAll(
                titleLabel,
                themeSection,
                separator);

        getChildren().add(mainContainer);
    }

    private VBox createThemeSection() {
        VBox themeSection = new VBox(15);

        // Theme section title
        Label themeSectionTitle = new Label("Appearance");
        themeSectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Theme toggle row
        HBox themeRow = new HBox(15);
        themeRow.setAlignment(Pos.CENTER_LEFT);

        Label themeLabel = new Label("Theme:");
        themeLabel.setStyle("-fx-font-size: 14px;");
        themeLabel.setPrefWidth(100);

        Button themeButton = new Button();
        this.currentThemeButton = themeButton; // Store reference for theme change updates
        updateThemeButtonText(themeButton);
        currentThemeButton.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-padding: 8 16; " +
                        "-fx-background-radius: 6; " +
                        "-fx-border-radius: 6;");

        Button toggleThemeButton = new Button("Switch Theme");
        toggleThemeButton.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-padding: 8 16; " +
                        "-fx-background-color: #0096c9; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 6; " +
                        "-fx-border-radius: 6;");

        toggleThemeButton.setOnAction(e -> {
            themeManager.toggleTheme();
            updateThemeButtonText(themeButton);
        });

        // Add spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        themeRow.getChildren().addAll(
                themeLabel,
                themeButton,
                spacer,
                toggleThemeButton);

        // Theme description
        Label themeDescription = new Label(
                "Switch between light and dark themes. Your preference will be saved automatically.");
        themeDescription.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        themeDescription.setWrapText(true);
        themeDescription.setPrefWidth(500);

        themeSection.getChildren().addAll(
                themeSectionTitle,
                themeRow,
                themeDescription);

        return themeSection;
    }

    private void updateThemeButtonText(Button button) {
        String currentThemeName = themeManager.getCurrentTheme().getName();
        String displayName = currentThemeName.substring(0, 1).toUpperCase() + currentThemeName.substring(1);
        button.setText(displayName + " Theme");

        // Update button styling based on current theme
        if (themeManager.isDarkTheme()) {
            button.setStyle(
                    "-fx-font-size: 14px; " +
                            "-fx-padding: 8 16; " +
                            "-fx-background-color: #3c3c3c; " +
                            "-fx-text-fill: #e0e0e0; " +
                            "-fx-border-color: #555555; " +
                            "-fx-background-radius: 6; " +
                            "-fx-border-radius: 6;");
        } else {
            button.setStyle(
                    "-fx-font-size: 14px; " +
                            "-fx-padding: 8 16; " +
                            "-fx-background-color: #f5f5f5; " +
                            "-fx-text-fill: #333333; " +
                            "-fx-border-color: #cccccc; " +
                            "-fx-background-radius: 6; " +
                            "-fx-border-radius: 6;");
        }
    }

    @Override
    public void onThemeChanged(ThemeManager.Theme newTheme) {
        // Update the current theme button text and styling when theme changes
        if (currentThemeButton != null) {
            updateThemeButtonText(currentThemeButton);
        }
    }
}