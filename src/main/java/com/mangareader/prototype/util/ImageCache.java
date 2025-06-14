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

        // Validate URL format
        if (!isValidImageUrl(url)) {
            System.err.println("Invalid image URL: " + url);
            return getPlaceholderImage("Invalid+URL");
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

                // Validate cached file size
                if (Files.size(cachedFile) < 1024) {
                    System.err.println("Cached file too small, removing: " + filename);
                    Files.deleteIfExists(cachedFile);
                    return downloadAndCacheImage(url, cachedFile);
                }

                try {
                    Image cachedImage = new Image(new FileInputStream(cachedFile.toFile()));

                    // Check if cached image is corrupted
                    if (cachedImage.isError()) {
                        System.err.println("Cached image is corrupted, re-downloading: " + filename);
                        Files.deleteIfExists(cachedFile);
                        Image newImage = downloadAndCacheImage(url, cachedFile);
                        return newImage != null ? newImage : loadImage(url);
                    }

                    return cachedImage;
                } catch (Exception e) {
                    System.err.println("Error loading cached image, re-downloading: " + e.getMessage());
                    Files.deleteIfExists(cachedFile);
                    Image newImage = downloadAndCacheImage(url, cachedFile);
                    return newImage != null ? newImage : loadImage(url);
                }
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

            // Validate the downloaded file is not corrupted
            if (Files.size(cachedFile) < 1024) { // Too small to be a valid image
                System.err.println("Downloaded file too small, likely corrupted: " + url);
                Files.deleteIfExists(cachedFile);
                return null;
            }

            // Load and test the image
            Image testImage = new Image(new FileInputStream(cachedFile.toFile()));

            // Check if image loaded successfully
            if (testImage.isError()) {
                System.err.println("Downloaded image is corrupted: " + url);
                Files.deleteIfExists(cachedFile);
                return null;
            }

            return testImage;
        } catch (Exception e) {
            System.err.println("Error downloading and caching image: " + e.getMessage());
            // Clean up corrupted cache file
            try {
                Files.deleteIfExists(cachedFile);
            } catch (IOException cleanupError) {
                System.err.println("Error cleaning up corrupted cache file: " + cleanupError.getMessage());
            }
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

    private boolean isValidImageUrl(String url) {
        try {
            URL testUrl = new URL(url);
            String protocol = testUrl.getProtocol();
            return "http".equals(protocol) || "https".equals(protocol);
        } catch (Exception e) {
            return false;
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

            // Create image with better error handling
            Image image = new Image(url, 180, 270, true, true, true); // Load in background with error handling

            // Add error listener to handle corruption issues
            image.exceptionProperty().addListener((obs, oldEx, newEx) -> {
                if (newEx != null) {
                    System.err.println("Image loading exception for " + url + ": " + newEx.getMessage());
                    // Remove from cache if corrupted
                    memoryCache.remove(url);
                }
            });

            // Add error property listener for JPEG corruption
            image.errorProperty().addListener((obs, wasError, isError) -> {
                if (isError) {
                    System.err.println("Image error detected for " + url + " - likely corrupted JPEG data");
                    // Remove from cache if corrupted
                    memoryCache.remove(url);
                }
            });

            return image;
        } catch (Exception e) {
            System.err.println("Error loading image: " + url + " | " + e.getMessage());
            return new Image("https://via.placeholder.com/180x270?text=Error");
        }
    }
}
