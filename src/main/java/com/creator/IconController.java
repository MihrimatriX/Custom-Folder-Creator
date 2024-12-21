package com.creator;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.sf.image4j.codec.ico.ICOEncoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class IconController {
    private final List<File> fileList = new ArrayList<>();
    @FXML
    private Label dragDropLabel;
    @FXML
    private ListView<File> fileListView;
    @FXML
    private Label targetLabel;
    private File targetDirectory;

    @FXML
    public void initialize() {
        dragDropLabel.setOnDragOver(event -> {
            if (event.getGestureSource() != dragDropLabel && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        dragDropLabel.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                for (File file : db.getFiles()) {
                    if (file.getName().toLowerCase().endsWith(".png")) {
                        fileList.add(file);
                        fileListView.getItems().add(file);
                    }
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    @FXML
    private void handleSetTargetDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Hedef Klasörü Seç");
        Stage stage = (Stage) dragDropLabel.getScene().getWindow();
        File selectedDir = directoryChooser.showDialog(stage);
        if (selectedDir != null) {
            targetDirectory = selectedDir;
            targetLabel.setText("Hedef Klasör: " + selectedDir.getAbsolutePath());
        }
    }

    @FXML
    private void handleConvert() {
        if (fileList.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Hata", "Dönüştürülecek dosya yok.");
            return;
        }
        if (targetDirectory == null) {
            showAlert(Alert.AlertType.ERROR, "Hata", "Hedef klasör seçilmedi.");
            return;
        }

        for (File pngFile : fileList) {
            try {
                System.out.println("Converting: " + pngFile.getName());
                File targetFile = new File(targetDirectory, pngFile.getName().replace(".png", ".ico"));
                BufferedImage originalImage = ImageIO.read(new File(pngFile.getPath()));
                BufferedImage resizedImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = resizedImage.createGraphics();
                g.drawImage(originalImage, 0, 0, 256, 256, null);
                g.dispose();

                try (FileOutputStream icoOutputStream = new FileOutputStream(targetFile)) {
                    ICOEncoder.write(resizedImage, icoOutputStream);
                }
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Hata", pngFile.getName() + " dönüştürülürken bir hata oluştu.");
                ex.printStackTrace();
            }
        }
        showAlert(Alert.AlertType.INFORMATION, "Başarılı", "Tüm dosyalar dönüştürüldü!");
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}