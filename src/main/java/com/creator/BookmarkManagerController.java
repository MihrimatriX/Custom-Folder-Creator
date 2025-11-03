package com.creator;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookmarkManagerController {
    @FXML
    private ChoiceBox<String> browserList;
    @FXML
    private TextField sourceFolderField;
    @FXML
    private TextField titleTagField;
    @FXML
    private ListView<String> titleTagListView;
    @FXML
    private TableView<BookmarkInfo> bookmarkTableView;
    @FXML
    private TableColumn<BookmarkInfo, String> nameColumn;
    @FXML
    private TableColumn<BookmarkInfo, String> hdColumn;
    @FXML
    private TableColumn<BookmarkInfo, String> fullHdColumn;
    @FXML
    private TableColumn<BookmarkInfo, String> fourKColumn;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private TextArea logArea;
    @FXML
    private Button analyzeButton;
    @FXML
    private TableColumn<BookmarkInfo, String> recommendedColumn;

    private List<ChromeBookmarksReader.Bookmark> bookmarks;
    private int totalBookmarks = 0;
    private int processedBookmarks = 0;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final List<String> defaultTitleTags = Arrays.asList(
            "example_on",
            "example_fhd",
            "example_dortk",
            "example_dorkplus",
            "bilgi-icon");

    public static class BookmarkInfo {
        private final String name;
        private final String url;
        private String hdSize = "";
        private String fullHdSize = "";
        private String fourKSize = "";
        private boolean isDizi = false;
        private int seasonCount = 0;

        public BookmarkInfo(String name, String url) {
            this.name = cleanFileName(name);
            this.url = url;
            this.isDizi = url.contains("/dizi/");
        }

        private String cleanFileName(String name) {
            String cleaned = name;
            
            // Sadece gereksiz kısımları temizle, film adını koruyarak
            cleaned = cleaned
                .replaceAll("(?i)\\s*İndir\\s*-\\s*", " ")
                .replaceAll("\\|\\s*Film[^|]*$", "")
                .replaceAll("\\|\\s*Dizi[^|]*$", "")
                .replaceAll("(?i)\\s*Türkçe Dublaj\\s*", "")
                .replaceAll("(?i)\\s*DUAL\\s*", "")
                .replaceAll("(?i)\\s*ve Altyazı\\s*", "")
                .replaceAll("\\s+", " ")
                .trim();
            
            return cleaned;
        }

        public String getName() {
            if (isDizi) {
                return name + " (Dizi)";
            }
            return name;
        }

        public String getUrl() {
            return url;
        }

        public String getHdSize() {
            if (isDizi) {
                return seasonCount > 0 ? seasonCount + " Sezon" : "Dizi";
            }
            return formatSize(hdSize);
        }

        public String getFullHdSize() {
            if (isDizi) {
                return seasonCount > 0 ? seasonCount + " Sezon" : "Dizi";
            }
            return formatSize(fullHdSize);
        }

        public String getFourKSize() {
            if (isDizi) {
                return seasonCount > 0 ? seasonCount + " Sezon" : "Dizi";
            }
            return formatSize(fourKSize);
        }

        public boolean isDizi() {
            return isDizi;
        }

        public int getSeasonCount() {
            return seasonCount;
        }

        public void setHdSize(String size) {
            if (!isDizi) {
                this.hdSize = size;
            }
        }

        public void setFullHdSize(String size) {
            if (!isDizi) {
                this.fullHdSize = size;
            }
        }

        public void setFourKSize(String size) {
            if (!isDizi) {
                this.fourKSize = size;
            }
        }

        public void setSeasonCount(int count) {
            this.seasonCount = count;
        }

        private String formatSize(String size) {
            if (size.isEmpty())
                return "";
            return size;
        }
    }

    private void extractSizeInfo(Element element, BookmarkInfo bookmarkInfo) {
        String text = element.text();

        if (bookmarkInfo.isDizi() && text.matches(".*[Ss]ezon\\s+[Ss]ayısı\\s*:\\s*\\d+.*")) {
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                bookmarkInfo.setSeasonCount(Integer.parseInt(matcher.group()));
            }
            return;
        }

        if (text.contains("1080P")) {
            bookmarkInfo.setHdSize(extractSize(text));
        } else if (text.contains("Full HD")) {
            bookmarkInfo.setFullHdSize(extractSize(text));
        } else if (text.toLowerCase().contains("4k")) {
            bookmarkInfo.setFourKSize(extractSize(text));
        }
    }

    private String extractSize(String text) {
        Pattern pattern = Pattern.compile("(\\d+[.,]?\\d*)\\s*(GB|GiB)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1) + " " + matcher.group(2);
        }
        return "";
    }

    @FXML
    public void exportToExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Excel Dosyasını Kaydet");
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Excel Dosyaları", "*.xlsx"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Bookmarks");

                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Dosya Adı");
                headerRow.createCell(1).setCellValue("1080P");
                headerRow.createCell(2).setCellValue("Full HD");
                headerRow.createCell(3).setCellValue("4K");

                int rowNum = 1;
                for (BookmarkInfo info : bookmarkTableView.getItems()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(info.getName());
                    row.createCell(1).setCellValue(info.getHdSize());
                    row.createCell(2).setCellValue(info.getFullHdSize());
                    row.createCell(3).setCellValue(info.getFourKSize());
                }

                for (int i = 0; i < 4; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }

                log("Excel dosyası başarıyla kaydedildi: " + file.getName());
            } catch (IOException e) {
                log("Excel dosyası kaydedilirken hata oluştu: " + e.getMessage());
            }
        }
    }

    @FXML
    public void initialize() {
        browserList.getItems().addAll("Edge", "Chrome", "Firefox", "Safari");
        browserList.getSelectionModel().selectFirst();
        sourceFolderField.setText("Filmbol");

        progressBar.setProgress(0);
        log("Uygulama başlatıldı");
        resetTitleTags();

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        hdColumn.setCellValueFactory(new PropertyValueFactory<>("hdSize"));
        fullHdColumn.setCellValueFactory(new PropertyValueFactory<>("fullHdSize"));
        fourKColumn.setCellValueFactory(new PropertyValueFactory<>("fourKSize"));

        bookmarkTableView.setRowFactory(tv -> {
            TableRow<BookmarkInfo> row = new TableRow<>();
            row.setTooltip(new Tooltip());
            row.tooltipProperty().bind(javafx.beans.binding.Bindings.createObjectBinding(() -> {
                if (row.getItem() != null) {
                    return new Tooltip(row.getItem().getUrl());
                }
                return null;
            }, row.itemProperty()));
            return row;
        });

        logArea.setStyle("-fx-font-family: monospace;");
    }

    @FXML
    public void scanBookmarks() {
        if (sourceFolderField.getText().isEmpty()) {
            showAlert("Lütfen kaynak klasörü belirtin.");
            return;
        }

        bookmarks = ChromeBookmarksReader.readBookmarks(browserList.getValue(), sourceFolderField.getText());
        if (bookmarks.isEmpty()) {
            log("Belirtilen klasörde bookmark bulunamadı.");
            return;
        }

        bookmarkTableView.getItems().clear();
        for (ChromeBookmarksReader.Bookmark bookmark : bookmarks) {
            bookmarkTableView.getItems().add(new BookmarkInfo(bookmark.name(), bookmark.url()));
        }

        log(String.format("%d bookmark tarandı", bookmarks.size()));
        analyzeButton.setDisable(false);
    }

    @FXML
    public void loadAndAnalyzeBookmarks() {
        ObservableList<String> titleTags = titleTagListView.getItems();
        if (titleTags.isEmpty()) {
            showAlert("Lütfen en az bir class name ekleyin.");
            return;
        }

        totalBookmarks = bookmarks.size();
        processedBookmarks = 0;
        log(String.format("Toplam %d bookmark analiz edilecek", totalBookmarks));

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (ChromeBookmarksReader.Bookmark bookmark : bookmarks) {
            BookmarkInfo bookmarkInfo = bookmarkTableView.getItems().stream()
                    .filter(info -> info.url.equals(bookmark.url()))
                    .findFirst()
                    .orElse(new BookmarkInfo(bookmark.name(), bookmark.url()));

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    log("Analiz ediliyor: " + bookmark.name());
                    Document doc = Jsoup.connect(bookmark.url())
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                            .get();

                    for (String className : titleTags) {
                        for (Element element : doc.select(className)) {
                            extractSizeInfo(element, bookmarkInfo);
                        }
                    }
                    Platform.runLater(() -> bookmarkTableView.refresh());

                    processedBookmarks++;
                    updateProgressBar();
                    log("Tamamlandı: " + bookmark.name());
                } catch (IOException e) {
                    log("Hata (" + bookmark.name() + "): " + e.getMessage());
                }
            }, executorService);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        log("Tüm analizler tamamlandı!");
                        showCompletionAlert();
                    });
                    executorService.shutdown();
                });
    }

    @FXML
    public void addTitleTag() {
        String newTag = titleTagField.getText().trim();
        if (!newTag.isEmpty()) {
            if (!newTag.startsWith(".")) {
                newTag = "." + newTag;
            }
            if (!titleTagListView.getItems().contains(newTag)) {
                titleTagListView.getItems().add(newTag);
                titleTagField.clear();
                log("Yeni class name eklendi: " + newTag);
            }
        }
    }

    @FXML
    public void removeTitleTag() {
        String selectedTag = titleTagListView.getSelectionModel().getSelectedItem();
        if (selectedTag != null) {
            titleTagListView.getItems().remove(selectedTag);
            log("Class name silindi: " + selectedTag);
        }
    }

    @FXML
    public void resetTitleTags() {
        titleTagListView.setItems(FXCollections.observableArrayList());
        for (String tag : defaultTitleTags) {
            titleTagListView.getItems().add("." + tag);
        }
        log("Class name listesi varsayılana döndürüldü");
    }

    private void updateProgressBar() {
        if (totalBookmarks > 0) {
            double progress = (double) processedBookmarks / totalBookmarks;
            Platform.runLater(() -> progressBar.setProgress(progress));
        }
    }

    private void log(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String formattedMessage = String.format("[%s] %s%n", timestamp, message);

        Platform.runLater(() -> {
            String currentStyle = logArea.getStyle();

            logArea.appendText(formattedMessage);

            if (message.startsWith("Tamamlandı:")) {
                logArea.setStyle("-fx-font-family: monospace; -fx-text-fill: #90EE90;");
            } else {
                logArea.setStyle("-fx-font-family: monospace; -fx-text-fill: white;");
            }
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Uyarı");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showCompletionAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("İşlem Tamamlandı");
            alert.setHeaderText(null);
            alert.setContentText("Tüm bookmarkların analizi tamamlandı!");
            alert.showAndWait();
        });
    }
}