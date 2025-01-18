package com.creator;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

import static com.creator.WebScraper.fetchIcon;

public class AppController {
    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TableView<FileInfo> videoTableView;

    @FXML
    private TableColumn<FileInfo, String> nameColumn;

    @FXML
    private TableColumn<FileInfo, String> extensionColumn;

    @FXML
    private TableColumn<FileInfo, Long> sizeColumn;

    @FXML
    private ScrollPane detailsScrollPane;

    @FXML
    private VBox detailsVBox;

    @FXML
    private Label selectPathLabel;

    private String workDirectory;

    @FXML
    private VBox videoListVBox;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label progressLabel;

    @FXML
    private ProgressBar totalProgressBar;

    @FXML
    private Label totalProgressLabel;

    @FXML
    private void initialize() {
        String darkTheme = Objects.requireNonNull(App.class.getResource("/dark-theme.css")).toExternalForm();
        videoTableView.getParent().getStylesheets().add(darkTheme);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nameWithoutExtension"));
        extensionColumn.setCellValueFactory(new PropertyValueFactory<>("extension"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("sizeFormatted"));
        extensionColumn.setStyle("-fx-alignment: CENTER;");
        sizeColumn.setStyle("-fx-alignment: CENTER;");
        nameColumn.prefWidthProperty().bind(videoTableView.widthProperty().multiply(0.7));
        extensionColumn.prefWidthProperty().bind(videoTableView.widthProperty().multiply(0.09));
        sizeColumn.prefWidthProperty().bind(videoTableView.widthProperty().multiply(0.2));

        detailsScrollPane.setMaxHeight(400);
        detailsVBox.setMaxHeight(Region.USE_COMPUTED_SIZE);

        LogManager.getInstance().setLogTextFlow(scrollPane);
        LogManager.getInstance().addLog("App Started.", false);

        videoTableView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                FileInfo selectedFile = videoTableView.getSelectionModel().getSelectedItem();
                if (selectedFile != null) {
                    displayVideoDetails(new File(selectedFile.getPath()));
                } else {
                    clearDetails();
                }
            }
        });

        if (progressBar != null) {
            progressBar.setProgress(0);
            progressBar.setStyle("-fx-accent: #00ff00;");
        }
        if (progressLabel != null) {
            progressLabel.setText("Hazır");
            progressLabel.setStyle("-fx-text-fill: white;");
        }
        if (totalProgressBar != null) {
            totalProgressBar.setProgress(0);
            totalProgressBar.setStyle("-fx-accent: #00ff00;");
        }
        if (totalProgressLabel != null) {
            totalProgressLabel.setText("0/0");
            totalProgressLabel.setStyle("-fx-text-fill: white;");
        }
    }

    private void clearDetails() {
        detailsVBox.getChildren().clear();
        Label emptyLabel = new Label("No file selected");
        detailsVBox.getChildren().add(emptyLabel);
    }

    @FXML
    private void selectFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder");
        File selectedDirectory = directoryChooser.showDialog(null);
        
        if (selectedDirectory != null) {
            Path selectedFolder = selectedDirectory.toPath();
            String selectedFolderPath = selectedFolder.toString();
            listFiles(new File(selectedFolderPath));
            this.workDirectory = selectedFolderPath;
            selectPathLabel.setTextFill(Color.RED);
            selectPathLabel.setText(selectedFolderPath);
        }
    }

    @FXML
    private void executeWork() {
        if (workDirectory == null || workDirectory.isEmpty() || !isValidDirectory(workDirectory)) {
            showErrorAlert();
            return;
        }
        VideoOrganizer.execute(this.workDirectory.trim());
        fetchAndSaveIcons(videoTableView.getItems());
    }

    private boolean isValidDirectory(String path) {
        File directory = new File(path);
        return directory.exists() && directory.isDirectory();
    }

    private void showErrorAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Directory");
        alert.setHeaderText(null);
        alert.setContentText("Please select a valid work directory.");
        alert.showAndWait();
    }

    private void listFiles(File directory) {
        ObservableList<FileInfo> fileInfoList = FXCollections.observableArrayList();
        try (Stream<File> paths = Files.list(Paths.get(directory.toURI())).map(Path::toFile)) {
            paths.filter(File::isFile)
                    .filter(file -> file.getName().matches(".*\\.(mkv|mp4|avi|ts|mov|zip|rar|7z|iso|exe)$"))
                    .sorted((file1, file2) -> file1.getName().compareToIgnoreCase(file2.getName()))
                    .forEach(file -> {
                        String fileName = file.getName();
                        String fileExtension = getFileExtension(fileName);
                        long fileSize = file.length();
                        fileInfoList.add(new FileInfo(fileName, fileExtension, fileSize, file.getAbsolutePath()));
                    });

            paths.close();
            try (Stream<File> folderPaths = Files.list(Paths.get(directory.toURI())).map(Path::toFile)) {
                folderPaths.filter(File::isDirectory)
                        .sorted((file1, file2) -> file1.getName().compareToIgnoreCase(file2.getName()))
                        .forEach(folder -> {
                            String folderName = folder.getName();
                            fileInfoList.add(new FileInfo(folderName, "folder", 0, folder.getAbsolutePath()));
                        });
            }

            videoTableView.setItems(fileInfoList);
        } catch (Exception e) {
            LogManager.getInstance().addLog("Error from files listing.", true);
        }
    }

    private void displayVideoDetails(File file) {
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.Open(file.getAbsolutePath());
        detailsVBox.getChildren().clear();

        VBox generalBox = new VBox(5);
        TitledPane generalPane = new TitledPane("General", generalBox);
        generalPane.setExpanded(true);

        VBox videoBox = new VBox(5);
        TitledPane videoPane = new TitledPane("Video", videoBox);
        videoPane.setExpanded(true);

        addLabelToBox(generalBox, "File Name", file.getName());
        addLabelToBox(generalBox, "Format", mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Format"));
        addLabelToBox(generalBox, "File Size", mediaInfo.Get(MediaInfo.StreamKind.General, 0, "FileSize/String"));
        addLabelToBox(generalBox, "Duration", mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Duration/String"));
        addLabelToBox(generalBox, "Overall Bit Rate", mediaInfo.Get(MediaInfo.StreamKind.General, 0, "OverallBitRate/String"));
        addLabelToBox(generalBox, "Title", mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Title"));
        addLabelToBox(generalBox, "Album", mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Album"));
        addLabelToBox(generalBox, "Track Number", mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Track/Position"));
        addLabelToBox(generalBox, "Artist", mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Performer"));
        addLabelToBox(generalBox, "Composer", mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Composer"));
        addLabelToBox(generalBox, "Publisher", mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Publisher"));
        addLabelToBox(generalBox, "Genre", mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Genre"));
        addLabelToBox(generalBox, "Recorded Date", mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Recorded_Date"));
        addLabelToBox(generalBox, "Encoded Date", mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Encoded_Date"));
        addLabelToBox(generalBox, "Tagged Date", mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Tagged_Date"));
        addLabelToBox(generalBox, "Writing Application", mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Encoded_Application"));
        addLabelToBox(generalBox, "Writing Library", mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Encoded_Library"));
        addLabelToBox(generalBox, "Comment", mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Comment"));
        addLabelToBox(generalBox, "Description", mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Description"));
        addLabelToBox(generalBox, "Copyright", mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Copyright"));
        addLabelToBox(generalBox, "Language", mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Language"));
        addLabelToBox(generalBox, "Keywords", mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Keywords"));
        addLabelToBox(generalBox, "Producer", mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Producer"));
        addLabelToBox(generalBox, "Encoded By", mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Encoded_By"));

        addLabelToBox(videoBox, "Resolution", mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "Width") + " x " + mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "Height"));
        addLabelToBox(videoBox, "Frame Rate", mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "FrameRate/String"));
        addLabelToBox(videoBox, "Frame Count", mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "FrameCount"));
        addLabelToBox(videoBox, "Aspect Ratio", mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "DisplayAspectRatio/String"));
        addLabelToBox(videoBox, "Pixel Aspect Ratio", mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "PixelAspectRatio"));
        addLabelToBox(videoBox, "Scan Type", mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "ScanType"));
        addLabelToBox(videoBox, "Color Space", mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "ColorSpace"));
        addLabelToBox(videoBox, "Chroma Subsampling", mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "ChromaSubsampling"));
        addLabelToBox(videoBox, "HDR Format", mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "HDR_Format"));
        addLabelToBox(videoBox, "Color Primaries", mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "ColorPrimaries"));
        addLabelToBox(videoBox, "Transfer Characteristics", mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "Transfer_Characteristics"));
        addLabelToBox(videoBox, "Matrix Coefficients", mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "Matrix_Coefficients"));
        addLabelToBox(videoBox, "Bit Rate", mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "BitRate/String"));
        addLabelToBox(videoBox, "Codec", mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "CodecID"));
        addLabelToBox(videoBox, "Language", mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "Language"));
        addLabelToBox(videoBox, "Format Profile", mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "Format_Profile"));
        addLabelToBox(videoBox, "Encoded Library", mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "Encoded_Library/String"));

        VBox audioContainer = new VBox(10);
        int audioTrackCount = mediaInfo.Count_Get(MediaInfo.StreamKind.Audio);
        for (int i = 0; i < audioTrackCount; i++) {
            VBox audioTrackBox = new VBox(5);
            addLabelToBox(audioTrackBox, "Audio Format", mediaInfo.Get(MediaInfo.StreamKind.Audio, i, "Format"));
            addLabelToBox(audioTrackBox, "Channels", mediaInfo.Get(MediaInfo.StreamKind.Audio, i, "Channel(s)"));
            addLabelToBox(audioTrackBox, "Channel Layout", mediaInfo.Get(MediaInfo.StreamKind.Audio, i, "ChannelLayout"));
            addLabelToBox(audioTrackBox, "Channel Positions", mediaInfo.Get(MediaInfo.StreamKind.Audio, i, "ChannelPositions"));
            addLabelToBox(audioTrackBox, "Sampling Rate", mediaInfo.Get(MediaInfo.StreamKind.Audio, i, "SamplingRate/String"));
            addLabelToBox(audioTrackBox, "Bit Depth", mediaInfo.Get(MediaInfo.StreamKind.Audio, i, "BitDepth/String"));
            addLabelToBox(audioTrackBox, "Bit Rate", mediaInfo.Get(MediaInfo.StreamKind.Audio, i, "BitRate/String"));
            addLabelToBox(audioTrackBox, "Bit Rate Mode", mediaInfo.Get(MediaInfo.StreamKind.Audio, i, "BitRate_Mode"));
            addLabelToBox(audioTrackBox, "Compression Mode", mediaInfo.Get(MediaInfo.StreamKind.Audio, i, "Compression_Mode"));
            addLabelToBox(audioTrackBox, "Compression Ratio", mediaInfo.Get(MediaInfo.StreamKind.Audio, i, "Compression_Ratio"));
            addLabelToBox(audioTrackBox, "Delay", mediaInfo.Get(MediaInfo.StreamKind.Audio, i, "Delay/String"));
            addLabelToBox(audioTrackBox, "Stream Size", mediaInfo.Get(MediaInfo.StreamKind.Audio, i, "StreamSize/String"));
            addLabelToBox(audioTrackBox, "Duration", mediaInfo.Get(MediaInfo.StreamKind.Audio, i, "Duration/String"));
            addLabelToBox(audioTrackBox, "Language", mediaInfo.Get(MediaInfo.StreamKind.Audio, i, "Language"));

            TitledPane audioPane = new TitledPane("Audio Track " + (i + 1), audioTrackBox);
            audioPane.setExpanded(true);
            audioContainer.getChildren().add(audioPane);
        }

        VBox textContainer = new VBox(10);
        int textTrackCount = mediaInfo.Count_Get(MediaInfo.StreamKind.Text);
        for (int i = 0; i < textTrackCount; i++) {
            VBox textTrackBox = new VBox(5);
            addLabelToBox(textTrackBox, "Text Format", mediaInfo.Get(MediaInfo.StreamKind.Text, i, "Format"));
            addLabelToBox(textTrackBox, "Language", mediaInfo.Get(MediaInfo.StreamKind.Text, i, "Language"));
            addLabelToBox(textTrackBox, "Codec", mediaInfo.Get(MediaInfo.StreamKind.Text, i, "CodecID"));
            addLabelToBox(textTrackBox, "Title", mediaInfo.Get(MediaInfo.StreamKind.Text, i, "Title"));
            addLabelToBox(textTrackBox, "Delay", mediaInfo.Get(MediaInfo.StreamKind.Text, i, "Delay/String"));
            addLabelToBox(textTrackBox, "Stream Size", mediaInfo.Get(MediaInfo.StreamKind.Text, i, "StreamSize/String"));
            addLabelToBox(textTrackBox, "Encoding", mediaInfo.Get(MediaInfo.StreamKind.Text, i, "Encoding"));
            addLabelToBox(textTrackBox, "Format Info", mediaInfo.Get(MediaInfo.StreamKind.Text, i, "Format/Info"));

            TitledPane textPane = new TitledPane("Subtitle Track " + (i + 1), textTrackBox);
            textPane.setExpanded(true);
            textContainer.getChildren().add(textPane);
        }

        VBox mainContainer = new VBox(10);
        mainContainer.getChildren().addAll(generalPane, videoPane, new TitledPane("Audio Tracks", audioContainer), new TitledPane("Subtitle Tracks", textContainer));
        detailsVBox.getChildren().add(mainContainer);
        mediaInfo.Close();
    }

    private void fetchAndSaveIcons(ObservableList<FileInfo> fileInfoList) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                int total = fileInfoList.size();
                int current = 0;

                Platform.runLater(() -> {
                    totalProgressBar.setProgress(0);
                    totalProgressLabel.setText("0/" + total);
                });

                for (FileInfo fileInfo : fileInfoList) {
                    current++;
                    final int currentFile = current;
                    
                    String fileName = fileInfo.getNameWithoutExtension();
                    String targetDirectory = workDirectory + "/" + fileName;

                    File dir = new File(targetDirectory);
                    if (!dir.exists()) {
                        var ignored = dir.mkdirs();
                    }

                    Platform.runLater(() -> {
                        progressLabel.setText("İşleniyor: " + fileName);
                        totalProgressLabel.setText(currentFile + "/" + total);
                        totalProgressBar.setProgress((double) currentFile / total);
                    });

                    fetchIcon(fileName, targetDirectory, progressBar);

                    Platform.runLater(() -> {
                        File iconFile = new File(targetDirectory + "/" + fileName + ".png");
                        if (iconFile.exists()) {
                            ImageView imageView = new ImageView(new Image(iconFile.toURI().toString()));
                            imageView.setFitHeight(128);
                            imageView.setFitWidth(120);

                            Label nameLabel = new Label(fileName);
                            nameLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bolder; -fx-padding: 5px;");

                            HBox itemBox = new HBox(15, imageView, nameLabel);
                            itemBox.setAlignment(Pos.CENTER_LEFT);
                            itemBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
                            videoListVBox.getChildren().add(itemBox);
                        }
                    });
                }
                return null;
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            progressBar.setProgress(1.0);
            progressLabel.setText("İşlem tamamlandı!");
            totalProgressBar.setProgress(1.0);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("İşlem Tamamlandı");
            alert.setHeaderText(null);
            alert.setContentText("Tüm dosyalar başarıyla oluşturuldu.");
            alert.showAndWait();

            try {
                File dirToOpen = new File(workDirectory);
                if (dirToOpen.exists()) {
                    Desktop.getDesktop().open(dirToOpen);
                }
            } catch (Exception e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Hata");
                errorAlert.setHeaderText("Klasör açılamadı");
                errorAlert.setContentText("Klasör açılırken bir hata oluştu.");
                errorAlert.showAndWait();
            }
        }));

        new Thread(task).start();
    }

    private void addLabelToBox(VBox box, String label, String value) {
        if (value == null || value.equalsIgnoreCase("N/A") || value.isEmpty()) {
            return;
        }

        Label infoLabel = new Label(label + ":");
        infoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 5 0 2 10;");

        Label valueLabel = new Label(value);
        valueLabel.setWrapText(true);
        valueLabel.setStyle("-fx-padding: 2 0 5 20; -fx-font-size: 12px;");

        HBox box1 = new HBox();
        box1.getChildren().addAll(infoLabel, valueLabel);
        box.getChildren().add(box1);
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
}