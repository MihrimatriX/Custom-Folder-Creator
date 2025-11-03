package com.creator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HtmlHelper {
    public static void saveHtmlToFile(String htmlContent, String targetDirectory, String fileName, String type) throws IOException {
        Path filePath = Paths.get(targetDirectory, fileName + ".html");
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filePath.toFile()), StandardCharsets.UTF_8);
        writer.write(htmlContent);
        LogManager.getInstance().addLog("HTML content saved to " + fileName + " File Type: " + type, false);
    }
}