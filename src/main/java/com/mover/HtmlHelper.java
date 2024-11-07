package com.mover;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HtmlHelper {
    public static void saveHtmlToFile(String htmlContent, String fileName, String type) {
        Path filePath = Paths.get(fileName);
        Path directoryPath = filePath.getParent();
        try {
            if (directoryPath != null && !Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filePath.toFile()), StandardCharsets.UTF_8)) {
                writer.write(htmlContent);
                LogManager.getInstance().addLog("HTML content saved to " + filePath.toString() + " File Type: " + type, false);
            }
        } catch (IOException e) {
            LogManager.getInstance().addLog("HTML content not saved to " + filePath.toString() + " File Type: " + type, true);
        }
    }
}