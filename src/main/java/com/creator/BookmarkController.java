package com.creator;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.TextArea;

public class BookmarkController {
    List<ChromeBookmarksReader.Bookmark> bookmarks;
    List<String> failedUrls = new ArrayList<>();
    @FXML
    private ListView<ChromeBookmarksReader.Bookmark> bookmarkListView;
    @FXML
    private TextField bookmarkField;
    @FXML
    private ChoiceBox<String> browserList;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private TextArea logArea;
    
    private int totalBookmarks = 0;
    private int processedBookmarks = 0;

    private void updateProgressBar() {
        if (totalBookmarks > 0) {
            double progress = (double) processedBookmarks / totalBookmarks;
            Platform.runLater(() -> progressBar.setProgress(progress));
        }
    }

    private void log(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        Platform.runLater(() -> {
            logArea.appendText(String.format("[%s] %s%n", timestamp, message));
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    @FXML
    public void initialize() {
        bookmarkField.setText("Utils");
        browserList.getItems().addAll("Edge", "Chrome", "Firefox", "Safari");
        browserList.getSelectionModel().selectFirst();

        bookmarks = ChromeBookmarksReader.readBookmarks(browserList.getValue(), bookmarkField.getText());
        bookmarkListView.setOnMouseClicked(this::handleListClick);
        bookmarkListView.getItems().addAll(bookmarks);
        bookmarkListView.setCellFactory((ListView<ChromeBookmarksReader.Bookmark> bookmark) -> new ListCell<>() {
            @Override
            protected void updateItem(ChromeBookmarksReader.Bookmark bookmark, boolean empty) {
                super.updateItem(bookmark, empty);
                if (empty || bookmark == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(bookmark.name());
                    setTooltip(new Tooltip(bookmark.url()));
                }
            }
        });

        progressBar.setProgress(0);
        log("Uygulama başlatıldı");
    }

    @FXML
    public void handleListClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            ChromeBookmarksReader.Bookmark selectedBookmark = bookmarkListView.getSelectionModel().getSelectedItem();
            if (selectedBookmark != null) {
                try {
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        desktop.browse(new URI(selectedBookmark.url()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    public void loadBookmarks() {
        if (bookmarkField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Bookmarks Loaded");
            alert.setHeaderText("Bookmarks List");
            alert.setContentText("Please enter typo in your bookmarks folder.");
            alert.showAndWait();
            return;
        }

        bookmarks = ChromeBookmarksReader.readBookmarks(browserList.getValue(), bookmarkField.getText());
        bookmarkListView.getItems().clear();
        bookmarkListView.getItems().addAll(bookmarks);
        log("Bookmark listesi güncellendi: " + bookmarkField.getText());
    }

    @FXML
    public void saveHtmlPages() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Target Directory");
        File selectedDirectory = directoryChooser.showDialog(null);

        if (selectedDirectory != null) {
            String targetDirectory = selectedDirectory.getAbsolutePath();
            loadBookmarks();
            if (bookmarks != null && !bookmarks.isEmpty()) {
                startSavingTask(targetDirectory);
            } else {
                log("Kaydedilecek bookmark bulunamadı!");
            }
        } else {
            log("Hedef dizin seçilmedi.");
        }
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9.\\-]", "_");
    }

    private void saveFailedUrlsToJson() {
        if (!failedUrls.isEmpty()) {
            try (FileWriter writer = new FileWriter("failed_urls.json")) {
                Gson gson = new Gson();
                gson.toJson(failedUrls, writer);
                System.out.println("Failed URLs saved to failed_urls.json");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void embedResources(Document document) throws IOException {
        for (Element link : document.select("link[rel='stylesheet']")) {
            String cssUrl = link.absUrl("href");
            String cssContent = downloadFileContent(cssUrl);
            String encodedCss = "data:text/css;base64," + encodeBase64(cssContent);
            link.attr("href", encodedCss);
        }

        for (Element script : document.select("script[src]")) {
            String jsUrl = script.absUrl("src");
            String jsContent = downloadFileContent(jsUrl);
            String encodedJs = "data:application/javascript;base64," + encodeBase64(jsContent);
            script.attr("src", encodedJs);
        }

        for (Element img : document.select("img[src]")) {
            String imgUrl = img.absUrl("src");
            img.attr("src", imgUrl);
        }
    }

    private String downloadFileContent(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        try (InputStream inputStream = url.openStream()) {
            return new String(inputStream.readAllBytes());
        }
    }

    private void saveHtmlWithEmbeddedResources(Document document, String targetDirectory, String name) throws IOException {
        String fileName = sanitizeFileName(name) + ".html";
        embedResources(document);
        Path htmlPath = Paths.get(targetDirectory, fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(htmlPath, StandardCharsets.UTF_8)) {
            writer.write(document.html());
        }
    }

    private String encodeBase64(String content) {
        return Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
    }

    private javafx.concurrent.Task<Void> createHttrackTask(ChromeBookmarksReader.Bookmark bookmark, String targetDirectory) {
        return new javafx.concurrent.Task<>() {
            @Override
            protected Void call() {
                log("İndirme başlatıldı: " + bookmark.name());
                boolean success = savePageWithHttrack(bookmark, targetDirectory);
                if (!success) {
                    log("HTTrack başarısız oldu, JSoup ile deneniyor: " + bookmark.name());
                    savePageWithHttrackAndJsoup(bookmark, targetDirectory);
                }
                processedBookmarks++;
                Platform.runLater(() -> updateProgressBar());
                return null;
            }
        };
    }

    private boolean savePageWithHttrack(ChromeBookmarksReader.Bookmark bookmark, String targetDirectory) {
        String httrackPath = "\"C:\\Apps\\WinHTTrack\\httrack.exe\"";
        String sanitizedName = sanitizeFileName(bookmark.name());
        String httrackCommand = String.format("%s \"%s\" -O \"%s\\%s\" -r1",
                httrackPath, bookmark.url(), targetDirectory, sanitizedName);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", httrackCommand);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("HTTrack successfully saved: " + bookmark.url());
                return true;
            } else {
                System.err.println("HTTrack failed with exit code: " + exitCode);
            }
        } catch (Exception e) {
            System.err.println("HTTrack command execution failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private void savePageWithHttrackAndJsoup(ChromeBookmarksReader.Bookmark bookmark, String targetDirectory) {
        try {
            Document document = Jsoup
                    .connect(bookmark.url())
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .get();
            saveHtmlWithEmbeddedResources(document, targetDirectory, bookmark.name());
            System.out.println("Saved with HTTrack and JSoup: " + bookmark.url());
        } catch (IOException e) {
            System.out.println("JSoup failed to save: " + bookmark.url());
            failedUrls.add(bookmark.url());
            saveFailedUrlsToJson();
        }
    }

    private void startSavingTask(String targetDirectory) {
        List<javafx.concurrent.Task<Void>> tasks = new ArrayList<>();
        totalBookmarks = bookmarks.size();
        processedBookmarks = 0;
        log("Toplam " + totalBookmarks + " bookmark indirilecek");

        for (ChromeBookmarksReader.Bookmark bookmark : bookmarks) {
            javafx.concurrent.Task<Void> task = createHttrackTask(bookmark, targetDirectory);
            tasks.add(task);
            task.setOnSucceeded(worker -> {
                log("Başarıyla kaydedildi: " + bookmark.name());
            });
            task.setOnFailed(worker -> {
                log("Kaydetme başarısız: " + bookmark.name());
                failedUrls.add(bookmark.url());
            });
        }

        new Thread(() -> {
            tasks.forEach(task -> {
                try {
                    new Thread(task).start();
                    task.get();
                } catch (Exception e) {
                    Platform.runLater(() -> log("Hata: " + e.getMessage()));
                    e.printStackTrace();
                }
            });
            Platform.runLater(() -> {
                saveFailedUrlsToJson();
                log("Tüm işlemler tamamlandı!");
            });
        }).start();
    }
}