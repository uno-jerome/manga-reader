package com.mangareader.prototype.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javafx.scene.image.Image;

/**
 * Image cache utility to prevent reloading covers when theme changes
 * Supports both memory and disk caching for better performance
 */
public class ImageCache {
    private static final ImageCache instance = new ImageCache();
    private final Map<String, Image> memoryCache = new ConcurrentHashMap<>();
    private final Path cacheDir;
    private final boolean diskCacheEnabled;

    private ImageCache() {
        // Initialize disk cache directory
        String projectDir = System.getProperty("user.dir");
        this.cacheDir = Paths.get(projectDir, "cache", "images");

        // Try to create cache directory
        boolean cacheCreated = false;
        try {
            Files.createDirectories(cacheDir);
            cacheCreated = true;
            System.out.println("Image cache directory created at: " + cacheDir.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to create image cache directory: " + e.getMessage());
        }

        this.diskCacheEnabled = cacheCreated;
    }

    public static ImageCache getInstance() {
        return instance;
    }

    /**
     * Get cached image or load and cache if not present
     */
    public Image getImage(String url) {
        if (url == null || url.isEmpty()) {
            return getPlaceholderImage("No+Cover");
        }

        return memoryCache.computeIfAbsent(url, this::loadImageWithDiskCache);
    }

    /**
     * Get a placeholder image for errors or missing covers
     */
    public Image getPlaceholderImage(String text) {
        String placeholderUrl = "https://via.placeholder.com/180x270?text=" + text;
        return memoryCache.computeIfAbsent(placeholderUrl, this::loadImage);
    }

    /**
     * Clear the memory cache
     */
    public void clearMemoryCache() {
        memoryCache.clear();
    }

    /**
     * Clear the entire cache (memory and disk)
     */
    public void clearCache() {
        clearMemoryCache();
        if (diskCacheEnabled) {
            clearDiskCache();
        }
    }

    /**
     * Remove specific image from cache
     */
    public void removeFromCache(String url) {
        memoryCache.remove(url);
        if (diskCacheEnabled) {
            try {
                String filename = getCacheFileName(url);
                Path cachedFile = cacheDir.resolve(filename);
                Files.deleteIfExists(cachedFile);
            } catch (Exception e) {
                System.err.println("Error removing cached file: " + e.getMessage());
            }
        }
    }

    /**
     * Get cache size for debugging
     */
    public int getMemoryCacheSize() {
        return memoryCache.size();
    }

    /**
     * Get disk cache size for debugging
     */
    public long getDiskCacheSize() {
        if (!diskCacheEnabled)
            return 0;

        try {
            return Files.walk(cacheDir)
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .sum();
        } catch (IOException e) {
            return 0;
        }
    }

    private Image loadImageWithDiskCache(String url) {
        if (!diskCacheEnabled) {
            return loadImage(url);
        }

        try {
            String filename = getCacheFileName(url);
            Path cachedFile = cacheDir.resolve(filename);

            // Check if file exists in disk cache
            if (Files.exists(cachedFile)) {
                System.out.println("Loading image from disk cache: " + filename);
                return new Image(new FileInputStream(cachedFile.toFile()));
            } else {
                // Download and cache to disk
                System.out.println("Downloading and caching image: " + url);
                Image image = downloadAndCacheImage(url, cachedFile);
                return image != null ? image : loadImage(url);
            }
        } catch (Exception e) {
            System.err.println("Error with disk cache, falling back to direct load: " + e.getMessage());
            return loadImage(url);
        }
    }

    private Image downloadAndCacheImage(String url, Path cachedFile) {
        try {
            // Download image to cache directory
            URL imageUrl = new URL(url);
            try (ReadableByteChannel rbc = Channels.newChannel(imageUrl.openStream());
                    FileOutputStream fos = new FileOutputStream(cachedFile.toFile())) {
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }

            // Load and return image
            return new Image(new FileInputStream(cachedFile.toFile()));
        } catch (Exception e) {
            System.err.println("Error downloading and caching image: " + e.getMessage());
            return null;
        }
    }

    private String getCacheFileName(String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(url.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString() + ".jpg";
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple hash
            return String.valueOf(url.hashCode()) + ".jpg";
        }
    }

    private void clearDiskCache() {
        try {
            Files.walk(cacheDir)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.err.println("Error deleting cached file: " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error clearing disk cache: " + e.getMessage());
        }
    }

    private Image loadImage(String url) {
        try {
            System.out.println("Loading and caching image: " + url);
            return new Image(url, true); // Load in background
        } catch (Exception e) {
            System.err.println("Error loading image: " + url + " | " + e.getMessage());
            return new Image("https://via.placeholder.com/180x270?text=Error");
        }
    }
}
