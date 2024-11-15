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
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/archive.png")).toString()));
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/app.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("File AutoRun File Creator");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }
}