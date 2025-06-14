package com.mangareader.prototype.service;

import java.util.List;
import java.util.Optional;

import com.mangareader.prototype.model.Manga;

/**
 * Service for managing the user's manga library
 * Provides clean separation between library management and external manga
 * sources
 */
public interface LibraryService {

    /**
     * Get all manga in the user's library
     * 
     * @return List of manga in library (empty list if no manga added)
     */
    List<Manga> getLibrary();

    /**
     * Add a manga to the user's library
     * 
     * @param manga The manga to add
     * @return true if successfully added, false if already exists
     */
    boolean addToLibrary(Manga manga);

    /**
     * Remove a manga from the user's library
     * 
     * @param mangaId The ID of the manga to remove
     * @return true if successfully removed, false if not found
     */
    boolean removeFromLibrary(String mangaId);

    /**
     * Check if a manga is in the library
     * 
     * @param mangaId The ID of the manga to check
     * @return true if manga is in library
     */
    boolean isInLibrary(String mangaId);

    /**
     * Get a specific manga from the library
     * 
     * @param mangaId The ID of the manga
     * @return Optional containing the manga if found
     */
    Optional<Manga> getLibraryManga(String mangaId);

    /**
     * Update reading progress for a manga in the library
     * 
     * @param mangaId       The ID of the manga
     * @param chaptersRead  Number of chapters read
     * @param totalChapters Total number of chapters
     */
    void updateReadingProgress(String mangaId, int chaptersRead, int totalChapters);

    /**
     * Update the current reading position for a specific chapter
     * 
     * @param mangaId    The ID of the manga
     * @param chapterId  The ID of the current chapter
     * @param pageNumber The current page number (0-based)
     * @param totalPages Total pages in the chapter
     */
    void updateReadingPosition(String mangaId, String chapterId, int pageNumber, int totalPages);

    /**
     * Get the current reading position for a manga
     * 
     * @param mangaId The ID of the manga
     * @return Optional containing the reading position if found
     */
    Optional<ReadingPosition> getReadingPosition(String mangaId);

    /**
     * Mark a chapter as read
     * 
     * @param mangaId   The ID of the manga
     * @param chapterId The ID of the chapter
     */
    void markChapterAsRead(String mangaId, String chapterId);

    /**
     * Get reading progress percentage for a manga
     * 
     * @param mangaId The ID of the manga
     * @return Progress percentage (0.0 to 1.0)
     */
    double getReadingProgress(String mangaId);

    /**
     * Clear the entire library (for testing/reset purposes)
     */
    void clearLibrary();

    /**
     * Get library statistics
     * 
     * @return LibraryStats object containing various statistics
     */
    LibraryStats getLibraryStats();

    /**
     * Search within the user's library
     * 
     * @param query Search query
     * @return List of matching manga from library
     */
    List<Manga> searchLibrary(String query);

    /**
     * Library statistics
     */
    class LibraryStats {
        private final int totalManga;
        private final int readingManga;
        private final int completedManga;
        private final int plannedManga;

        public LibraryStats(int totalManga, int readingManga, int completedManga, int plannedManga) {
            this.totalManga = totalManga;
            this.readingManga = readingManga;
            this.completedManga = completedManga;
            this.plannedManga = plannedManga;
        }

        public int getTotalManga() {
            return totalManga;
        }

        public int getReadingManga() {
            return readingManga;
        }

        public int getCompletedManga() {
            return completedManga;
        }

        public int getPlannedManga() {
            return plannedManga;
        }
    }

    /**
     * Reading position tracking for auto-resume functionality
     */
    class ReadingPosition {
        private final String mangaId;
        private final String chapterId;
        private final int pageNumber;
        private final int totalPages;
        private final java.time.LocalDateTime lastUpdated;

        public ReadingPosition(String mangaId, String chapterId, int pageNumber, int totalPages,
                java.time.LocalDateTime lastUpdated) {
            this.mangaId = mangaId;
            this.chapterId = chapterId;
            this.pageNumber = pageNumber;
            this.totalPages = totalPages;
            this.lastUpdated = lastUpdated;
        }

        public String getMangaId() {
            return mangaId;
        }

        public String getChapterId() {
            return chapterId;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public java.time.LocalDateTime getLastUpdated() {
            return lastUpdated;
        }

        public double getChapterProgress() {
            return totalPages > 0 ? (double) pageNumber / totalPages : 0.0;
        }
    }
}
