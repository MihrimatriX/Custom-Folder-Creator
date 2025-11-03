package com.creator;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DirectoryController {
    @FXML
    private TableView<FileInfo> tableView;

    @FXML
    private TableColumn<FileInfo, String> nameColumn;

    @FXML
    private TableColumn<FileInfo, String> typeColumn;

    @FXML
    private TableColumn<FileInfo, String> sizeColumn;

    @FXML
    private TableColumn<FileInfo, Integer> dvd8Column;

    @FXML
    private TableColumn<FileInfo, Integer> dvd4Column;

    @FXML
    private void handleResetAction() {
        tableView.getItems().clear();
        System.out.println("Sistem sıfırlandı. Yeni bir sürükleme yapılabilir.");
    }

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        sizeColumn.setCellValueFactory(cellData -> cellData.getValue().sizeProperty());
        dvd8Column.setCellValueFactory(cellData -> cellData.getValue().dvd8Property().asObject());
        dvd4Column.setCellValueFactory(cellData -> cellData.getValue().dvd4Property().asObject());
    }

    private void showAlert(Alert.AlertType alertType, String title, String headerText, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    private List<FileInfo> calculateFolderSizes(File folder) {
        List<FileInfo> fileInfoList = new ArrayList<>();
        try {
            for (File file : Objects.requireNonNull(folder.listFiles())) {
                if (!file.canRead() || isSystemFolder(file)) {
                    continue;
                }

                long size = calculateSize(file);
                String displayName = file.getName();
                String extension = getFileExtension(file);
                String type = file.isDirectory() ? "Folder" : (extension.isEmpty() ? "No Extension" : extension);

                int dvd8Count = (int) (size / (8L * 1024 * 1024 * 1024));
                long remainingSize = size % (8L * 1024 * 1024 * 1024);

                int dvd4Count = 0;

                if (remainingSize > 0) {
                    if (remainingSize <= 4L * 1024 * 1024 * 1024) {
                        dvd4Count = 1;
                    } else {
                        dvd8Count += 1;
                    }
                }
                fileInfoList.add(new FileInfo(displayName, size, dvd8Count, dvd4Count, type));
            }
        } catch (Exception e) {
            System.out.println("Klasör boyutları hesaplanırken hata oluştu: " + e.getMessage());
        }
        return fileInfoList;
    }

    private boolean isSystemFolder(File file) {
        return file.getAbsolutePath().toLowerCase().contains("$recycle.bin") || file.getAbsolutePath().toLowerCase().contains("system volume information");
    }

    private String getFileExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    private long calculateSize(File file) {
        if (file.isFile()) {
            return file.length();
        } else if (file.isDirectory()) {
            long size = 0;
            File[] files = file.listFiles();
            if (files != null) {
                for (File subFile : files) {
                    size += calculateSize(subFile);
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Hata", "Klasör Boyutları Hesaplanırken Hata", "Kalsör Boyutları Hesaplanamadı");
            }
            return size;
        }
        return 0;
    }

    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getGestureSource() != tableView && event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    @FXML
    private void handleDragDropped(DragEvent event) {
        try {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                File folder = db.getFiles().get(0);//.getFirst();
                if (folder.isDirectory() || folder.getParent() == null) {
                    List<FileInfo> fileInfoList = calculateFolderSizes(folder);
                    tableView.getItems().setAll(fileInfoList);
                    success = true;
                } else {
                    showAlert(Alert.AlertType.WARNING, "Uyarı", "Geçersiz Öğe", "Sürüklenecek öğe bir klasör veya sürücü değil: " + folder.getAbsolutePath());
                }
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Bilgi", "Dragboard Boş", "Dragboard'da dosya yok.");
            }

            event.setDropCompleted(success);
            event.consume();
        } catch (Exception e) {
            showAlert(Alert.AlertType.INFORMATION, "Bilgi", "Dragboard Boş", "Dragboard'da bir hata meydana geldi.");
        }
    }

    @FXML
    private void exportToExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export to Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(tableView.getScene().getWindow());

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Directory Analysis");

                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Name");
                headerRow.createCell(1).setCellValue("Size");
                headerRow.createCell(2).setCellValue("8 GB DVDs");
                headerRow.createCell(3).setCellValue("4 GB DVDs");

                List<FileInfo> fileList = tableView.getItems();

                int totalDvd8Count = 0;
                int totalDvd4Count = 0;

                for (int i = 0; i < fileList.size(); i++) {
                    FileInfo fileInfo = fileList.get(i);
                    Row row = sheet.createRow(i + 1);
                    row.createCell(0).setCellValue(fileInfo.nameProperty().get());
                    row.createCell(1).setCellValue(fileInfo.sizeProperty().get());
                    row.createCell(2).setCellValue(fileInfo.dvd8Property().get());
                    row.createCell(3).setCellValue(fileInfo.dvd4Property().get());

                    totalDvd8Count += fileInfo.dvd8Property().get();
                    totalDvd4Count += fileInfo.dvd4Property().get();
                }

                int summaryRowIndex = fileList.size() + 1;
                Row summaryRow = sheet.createRow(summaryRowIndex);
                summaryRow.createCell(0).setCellValue("Total");
                summaryRow.createCell(2).setCellValue(totalDvd8Count);
                summaryRow.createCell(3).setCellValue(totalDvd4Count);

                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }

                showAlert(Alert.AlertType.INFORMATION, "Başarılı", "Excel Dosyası Oluşturuldu", "Dosya: " + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Hata", "Excel Dosyası Oluşturulamadı", e.getMessage());
            }
        }
    }

    private static class FileInfo {
        private final StringProperty name;
        private final StringProperty size;
        private final IntegerProperty dvd8Count;
        private final IntegerProperty dvd4Count;
        private final StringProperty type;

        public FileInfo(String name, long sizeInBytes, int dvd8Count, int dvd4Count, String type) {
            this.name = new SimpleStringProperty(name);
            this.size = new SimpleStringProperty(formatSize(sizeInBytes));
            this.dvd8Count = new SimpleIntegerProperty(dvd8Count);
            this.dvd4Count = new SimpleIntegerProperty(dvd4Count);
            this.type = new SimpleStringProperty(type);
        }

        public StringProperty nameProperty() {
            return name;
        }

        public StringProperty sizeProperty() {
            return size;
        }

        public IntegerProperty dvd8Property() {
            return dvd8Count;
        }

        public IntegerProperty dvd4Property() {
            return dvd4Count;
        }

        public StringProperty typeProperty() {
            return type;
        }

        private String formatSize(long sizeInBytes) {
            if (sizeInBytes >= 1_073_741_824) {
                return String.format("%.2f GB", sizeInBytes / 1_073_741_824.0);
            } else if (sizeInBytes >= 1_048_576) {
                return String.format("%.2f MB", sizeInBytes / 1_048_576.0);
            } else {
                return String.format("%.2f KB", sizeInBytes / 1024.0);
            }
        }
    }
}