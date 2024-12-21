package com.creator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader webScraperLoad = new FXMLLoader(App.class.getResource("/app.fxml"));
        FXMLLoader icoConvertLoad = new FXMLLoader(App.class.getResource("/ico_convert.fxml"));
        FXMLLoader directoryListLoad = new FXMLLoader(App.class.getResource("/directory_list.fxml"));
        FXMLLoader bookmarkSaverLoad = new FXMLLoader(App.class.getResource("/bookmark_saver.fxml"));

        Stage webScraperStage = new Stage();
        Scene webScraperScene = new Scene(webScraperLoad.load());
        webScraperScene.getStylesheets().add(Objects.requireNonNull(App.class.getResource("/dark-theme.css")).toExternalForm());
        webScraperStage.initOwner(null);
        webScraperStage.initStyle(StageStyle.DECORATED);
        webScraperStage.setResizable(false);
        webScraperStage.setTitle("File AutoRun File Creator");
        webScraperStage.setScene(webScraperScene);
        webScraperStage.show();

        Stage directoryListStage = new Stage();
        Scene directoryListScene = new Scene(directoryListLoad.load());
        directoryListScene.getStylesheets().add(Objects.requireNonNull(App.class.getResource("/dark-theme.css")).toExternalForm());
        directoryListStage.initOwner(null);
        directoryListStage.initStyle(StageStyle.DECORATED);
        directoryListStage.setResizable(false);
        directoryListStage.setTitle("Directory List");
        directoryListStage.setScene(directoryListScene);
        directoryListStage.show();

        Stage icoConvertStage = new Stage();
        Scene icoConvertScene = new Scene(icoConvertLoad.load());
        icoConvertScene.getStylesheets().add(Objects.requireNonNull(App.class.getResource("/dark-theme.css")).toExternalForm());
        icoConvertStage.initOwner(null);
        icoConvertStage.initStyle(StageStyle.DECORATED);
        icoConvertStage.setResizable(false);
        icoConvertStage.setTitle("Png ICO Converter");
        icoConvertStage.setScene(icoConvertScene);
        icoConvertStage.show();

        Stage bookmarkSaveStage = new Stage();
        Scene bookmarkSaveScene = new Scene(bookmarkSaverLoad.load());
        bookmarkSaveScene.getStylesheets().add(Objects.requireNonNull(App.class.getResource("/dark-theme.css")).toExternalForm());
        bookmarkSaveStage.initOwner(null);
        bookmarkSaveStage.initStyle(StageStyle.DECORATED);
        bookmarkSaveStage.setResizable(false);
        bookmarkSaveStage.setTitle("Bookmark Saver");
        bookmarkSaveStage.setScene(bookmarkSaveScene);
        bookmarkSaveStage.show();

        webScraperStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/archive.png")).toString()));
        directoryListStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/directory.png")).toString()));
        icoConvertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/icon.png")).toString()));
        bookmarkSaveStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/book.png")).toString()));

        webScraperStage.setX(250);
        webScraperStage.setY(200);

        directoryListStage.setX(webScraperStage.getX() + webScraperStage.getWidth() + 10);
        directoryListStage.setY(200);

        icoConvertStage.setX(webScraperStage.getX() + webScraperStage.getWidth() + 10);
        icoConvertStage.setY(directoryListStage.getY() + directoryListStage.getHeight() + 10);

        bookmarkSaveStage.setX(directoryListStage.getX() + directoryListStage.getWidth() + 10);
        bookmarkSaveStage.setY(200);
    }
}