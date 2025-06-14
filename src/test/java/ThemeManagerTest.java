package com.mangareader.prototype.ui;

import com.mangareader.prototype.ui.ThemeManager;
import com.mangareader.prototype.ui.ThemeManager.Theme;

/**
 * Simple test to verify ThemeManager functionality
 */
public class ThemeManagerTest {
    public static void main(String[] args) {
        System.out.println("=== ThemeManager Test ===");

        // Get instance
        ThemeManager themeManager = ThemeManager.getInstance();
        System.out.println("ThemeManager instance created successfully");

        // Test initial theme
        Theme initialTheme = themeManager.getCurrentTheme();
        System.out.println("Initial theme: " + initialTheme.getName());

        // Test theme colors
        System.out.println("Background color: " + themeManager.getBackgroundColor());
        System.out.println("Text color: " + themeManager.getTextColor());
        System.out.println("Border color: " + themeManager.getBorderColor());

        // Test theme toggle
        System.out.println("\n--- Testing theme toggle ---");
        themeManager.toggleTheme();
        Theme toggledTheme = themeManager.getCurrentTheme();
        System.out.println("After toggle: " + toggledTheme.getName());

        // Test theme colors after toggle
        System.out.println("Background color: " + themeManager.getBackgroundColor());
        System.out.println("Text color: " + themeManager.getTextColor());
        System.out.println("Border color: " + themeManager.getBorderColor());

        // Toggle back
        themeManager.toggleTheme();
        Theme finalTheme = themeManager.getCurrentTheme();
        System.out.println("After second toggle: " + finalTheme.getName());

        // Test theme listener
        System.out.println("\n--- Testing theme listener ---");
        themeManager.addThemeChangeListener(new ThemeManager.ThemeChangeListener() {
            @Override
            public void onThemeChanged(Theme newTheme) {
                System.out.println("Theme changed to: " + newTheme.getName());
            }
        });

        themeManager.setTheme(Theme.DARK);
        themeManager.setTheme(Theme.LIGHT);

        System.out.println("\n=== Test completed successfully ===");
    }
}
