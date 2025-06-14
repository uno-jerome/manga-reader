# MangaReader 📚

A modern, cross-platform manga reader application built with JavaFX that provides a seamless reading experience with support for multiple manga sources.

[![Java Version](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-blue.svg)](https://openjfx.io/)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](#)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](#)

## ✨ Features

- **📖 Clean Reading Interface**: Intuitive and modern UI designed for comfortable manga reading
- **🔍 Powerful Search**: Advanced search with filters for genres, status, and content rating
- **📚 Library Management**: Add manga to your personal library and track reading progress
- **🌐 Multiple Sources**: Currently supports MangaDx with extensible architecture for additional sources
- **⬇️ Download Support**: Download chapters for offline reading
- **🎯 Chapter Navigation**: Easy chapter browsing with multiple view modes (list, grid, volumes)
- **🎨 Modern UI**: Clean, responsive interface with dark/light theme support
- **📱 Cross-Platform**: Runs on Windows, macOS, and Linux

## 🚀 Quick Start

### Prerequisites

- **Java 17 or higher** - [Download here](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.6+** - [Installation guide](https://maven.apache.org/install.html)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/manga-reader.git
   cd manga-reader
   ```

2. **Build the project**
   ```bash
   mvn clean compile
   ```

3. **Run the application**
   ```bash
   mvn javafx:run
   ```

### Alternative: Direct Java Execution

```bash
# Compile first
mvn clean package

# Run with explicit module path (if needed)
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -cp target/classes com.mangareader.prototype.MangaReaderApplication
```

## 🏗️ Architecture

### Project Structure

```
manga-reader/
├── src/main/java/com/mangareader/prototype/
│   ├── MangaReaderApplication.java     # Main application entry point
│   ├── model/                          # Data models
│   │   ├── Chapter.java
│   │   ├── Manga.java
│   │   ├── SearchParams.java
│   │   └── SearchResult.java
│   ├── service/                        # Business logic layer
│   │   ├── MangaService.java
│   │   └── impl/
│   │       ├── DefaultMangaServiceImpl.java
│   │       └── MangaServiceImpl.java
│   ├── source/                         # Data source implementations
│   │   ├── MangaSource.java
│   │   └── impl/
│   │       └── MangaDexSource.java
│   └── ui/                            # User interface components
│       ├── MainView.java
│       ├── MangaDetailView.java
│       ├── MangaReaderView.java
│       ├── AddSeriesModal.java
│       └── ...
├── src/main/resources/
│   └── styles/
│       └── main.css                   # Application styling
└── pom.xml                           # Maven configuration
```

### Core Components

- **🎯 MangaSource**: Interface for manga data providers (MangaDx, etc.)
- **📊 MangaService**: Business logic for manga operations and library management
- **🎨 UI Components**: Modular JavaFX views for different application screens
- **💾 Data Models**: POJOs representing manga, chapters, and search results

## 🎮 Usage

### Searching for Manga

1. **Basic Search**: Use the search bar in the Add Series view
2. **Advanced Search**: Access filters for:
   - Genres (Action, Romance, Fantasy, etc.)
   - Publication status (Ongoing, Completed, Hiatus)
   - Content rating (Safe, Suggestive, NSFW)

### Reading Manga

1. **Browse Library**: View your collected manga in the library
2. **Select Manga**: Click on any manga to view details and chapters
3. **Start Reading**: Click "Start Reading" or select a specific chapter
4. **Navigation**: Use scroll or keyboard arrows to navigate pages

### Library Management

- **Add to Library**: Click the "Add to Library" button on any manga detail page
- **Track Progress**: Reading progress is automatically saved
- **Download Chapters**: Use the download button for offline reading

## 🔧 Configuration

### Data Storage

The application stores data in your user directory:
- **Windows**: `%USERPROFILE%\.houdoku\`
- **macOS/Linux**: `~/.houdoku/`

### Files Created

- `library.json` - Your manga library and reading progress
- `downloads/` - Downloaded manga chapters

## 🛠️ Development

### Building from Source

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package application
mvn package

# Run with development profile
mvn javafx:run
```

### Adding New Manga Sources

1. Implement the `MangaSource` interface
2. Add your implementation to the service layer
3. Register in `MangaServiceImpl` constructor

Example:
```java
public class NewMangaSource implements MangaSource {
    @Override
    public String getName() { return "New Source"; }
    
    @Override
    public List<Manga> search(String query, boolean includeNsfw) {
        // Implementation
    }
    
    // ... other methods
}
```

### Code Style

- Follow standard Java conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public APIs
- Maintain consistent indentation (4 spaces)

## 📋 Requirements

### System Requirements

- **Operating System**: Windows 10+, macOS 10.14+, or Linux
- **Memory**: 512MB RAM minimum, 1GB recommended
- **Storage**: 100MB free space
- **Network**: Internet connection for manga fetching

### Dependencies

| Dependency | Version | Purpose |
|-----------|---------|---------|
| JavaFX | 21.0.2 | User interface framework |
| Jackson | 2.16.1 | JSON processing |
| Apache HttpClient | 5.3 | HTTP requests |
| JUnit | 5.10.1 | Testing framework |

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit changes**: `git commit -m 'Add amazing feature'`
4. **Push to branch**: `git push origin feature/amazing-feature`
5. **Open a Pull Request**

### Development Guidelines

- Write tests for new features
- Update documentation as needed
- Follow existing code style
- Ensure all tests pass before submitting

## 🐛 Known Issues

- [ ] Download functionality needs complete implementation
- [ ] Some manga sources may have incomplete metadata
- [ ] Performance optimization needed for large libraries

## 🗺️ Roadmap

- [ ] **v1.1**: Complete download functionality
- [ ] **v1.2**: Additional manga sources (Mangakakalot, etc.)
- [ ] **v1.3**: Reading statistics and analytics
- [ ] **v1.4**: Cloud library synchronization
- [ ] **v2.0**: Mobile companion app

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **MangaDx** - For providing free manga API
- **JavaFX Community** - For excellent UI framework
- **Contributors** - Everyone who helps improve this project

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/yourusername/manga-reader/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/manga-reader/discussions)
- **Wiki**: [Project Wiki](https://github.com/yourusername/manga-reader/wiki)

---

<div align="center">
  <p>Made with ❤️ for manga lovers</p>
  <p>⭐ Star this repo if you find it helpful!</p>
</div>
