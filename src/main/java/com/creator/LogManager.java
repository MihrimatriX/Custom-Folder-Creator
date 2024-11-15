package com.creator;

import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class LogManager {
    private static LogManager instance;
    private static VBox logVBox;
    private static ScrollPane logScrollPane;

    private LogManager() {
    }

    public static LogManager getInstance() {
        if (instance == null) {
            instance = new LogManager();
            logVBox = new VBox(-5);
        }
        return instance;
    }

    public void setLogTextFlow(ScrollPane pane) {
        logScrollPane = pane;
    }

    public void addLog(String message, boolean isError) {
        Platform.runLater(() -> {
            Text text = new Text(message + "\n");
            text.setWrappingWidth(0);
            text.setStyle("-fx-font-size: 11px;");
            if (isError) {
                text.setFill(Color.RED);
            } else {
                text.setFill(Color.WHITE);
            }
            logVBox.getChildren().add(text);
            logScrollPane.setContent(logVBox);
        });
    }
}
