package com.mover;

import java.io.FileWriter;
import java.io.IOException;

public class HtmlHelper {
    public static void saveHtmlToFile(String htmlContent, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(htmlContent);
            System.out.println("HTML content saved to " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
