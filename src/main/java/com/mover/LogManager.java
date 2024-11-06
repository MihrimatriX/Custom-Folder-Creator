package com.mover;

import javafx.scene.control.TextArea;

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
    }

    public void addLog(String message) {
        if (logTextArea != null) {
            logTextArea.appendText(message + "\n");
        }
    }
}