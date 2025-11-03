package com.creator;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Modality;

import java.io.IOException;
import java.util.Objects;

public class MainMenuController {
    private Stage webScraperStage;
    private Stage directoryListStage;
    private Stage icoConvertStage;
    private Stage bookmarkSaveStage;
    private Stage bookmarkFilmbolStage;

    public void openWebScraper() throws IOException {
        if (webScraperStage == null || !webScraperStage.isShowing()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app.fxml"));
            webScraperStage = createStage(loader, "File AutoRun File Creator", "/archive.png");
            webScraperStage.setX(250);
            webScraperStage.setY(200);
            webScraperStage.show();
        } else {
            webScraperStage.requestFocus();
        }
    }

    public void openDirectoryList() throws IOException {
        if (directoryListStage == null || !directoryListStage.isShowing()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/directory_list.fxml"));
            directoryListStage = createStage(loader, "Directory List", "/directory.png");
            if (webScraperStage != null && webScraperStage.isShowing()) {
                directoryListStage.setX(webScraperStage.getX() + webScraperStage.getWidth() + 10);
                directoryListStage.setY(200);
            }
            directoryListStage.show();
        } else {
            directoryListStage.requestFocus();
        }
    }

    public void openIcoConverter() throws IOException {
        if (icoConvertStage == null || !icoConvertStage.isShowing()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ico_convert.fxml"));
            icoConvertStage = createStage(loader, "Png ICO Converter", "/icon.png");
            if (directoryListStage != null && directoryListStage.isShowing()) {
                icoConvertStage.setX(webScraperStage.getX() + webScraperStage.getWidth() + 10);
                icoConvertStage.setY(directoryListStage.getY() + directoryListStage.getHeight() + 10);
            }
            icoConvertStage.show();
        } else {
            icoConvertStage.requestFocus();
        }
    }

    public void openBookmarkSaver() throws IOException {
        if (bookmarkSaveStage == null || !bookmarkSaveStage.isShowing()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/bookmark_saver.fxml"));
            bookmarkSaveStage = createStage(loader, "Bookmark Saver", "/book.png");
            if (directoryListStage != null && directoryListStage.isShowing()) {
                bookmarkSaveStage.setX(directoryListStage.getX() + directoryListStage.getWidth() + 10);
                bookmarkSaveStage.setY(200);
            }
            bookmarkSaveStage.show();
        } else {
            bookmarkSaveStage.requestFocus();
        }
    }

    public void openBookmarkFilmbol() throws IOException {
        if (bookmarkFilmbolStage == null || !bookmarkFilmbolStage.isShowing()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/bookmark_manager.fxml"));
            bookmarkFilmbolStage = createStage(loader, "Bookmark Filmbol Analyze", "/book.png");
            bookmarkFilmbolStage.show();
        } else {
            bookmarkFilmbolStage.requestFocus();
        }
    }

    private Stage createStage(FXMLLoader loader, String title, String iconPath) throws IOException {
        Stage stage = new Stage();
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/dark-theme.css")).toExternalForm());
        stage.initStyle(StageStyle.DECORATED);
        stage.initOwner(null);
        stage.setResizable(false);
        stage.setTitle(title);
        stage.setScene(scene);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource(iconPath)).toString()));
        return stage;
    }
}