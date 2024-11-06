package com.mover;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class LogManager {
    private static LogManager instance;
    private TextArea logTextArea;

    private LogManager() {}

    public static LogManager getInstance() {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }

    public void setLogTextArea(TextArea textArea) {
        this.logTextArea = textArea;
        this.logTextArea.setWrapText(false);
    }

    public void addLog(String message, boolean isError) {
        Platform.runLater(() -> {
            if (logTextArea != null) {
                Text logText = new Text(message + " ");
                if (isError) {
                    logText.setFill(Color.RED);  // Hata mesajlarını kırmızı yap
                } else {
                    logText.setFill(Color.WHITE); // Diğer mesajları beyaz yap
                }

                logTextArea.appendText(logText.getText());
            }
        });
    }
}