package com.creator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader mainMenuLoader = new FXMLLoader(App.class.getResource("/main_menu.fxml"));
        Scene mainMenuScene = new Scene(mainMenuLoader.load());
        mainMenuScene.getStylesheets()
                .add(Objects.requireNonNull(App.class.getResource("/dark-theme.css")).toExternalForm());

        stage.setTitle("Custom Folder Creator");
        stage.setScene(mainMenuScene);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/archive.png")).toString()));
        stage.setResizable(false);
        stage.show();
    }
}