package com.mover;

import javafx.concurrent.Task;
import net.sf.image4j.codec.ico.ICOEncoder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.mover.HtmlHelper.saveHtmlToFile;

public class WebScraper {
    public static void fetchIcon(String fileName, String targetDirectory) {
        String baseUrl = "https://www.google.com";
        String searchUrl = baseUrl + "/search?q=" + fileName + URLEncoder.encode(" folder icon") + "&udm=2";

        try {
            Document searchPage = Jsoup.connect(searchUrl).get();
            System.out.println(targetDirectory);
            saveHtmlToFile(searchPage.html(), targetDirectory, "google html");
            Element iconElement = searchPage.select("div[data-attrid='images universal']").get(1);
            String deviantPageUrl = iconElement.attr("data-lpage");

            Document deviantPage = Jsoup.connect(deviantPageUrl).get();
            saveHtmlToFile(deviantPage.html(), targetDirectory, "deviant html");
            Element deviantFileIcon = deviantPage.selectFirst("img[property='contentUrl']");
            String deviantFileUrl = deviantFileIcon != null ? deviantFileIcon.attr("src") : "";

            if (!deviantFileUrl.isEmpty()) {
                String filePath = targetDirectory + "/" + fileName + ".png";
                downloadImage(deviantFileUrl, filePath);
                LogManager.getInstance().addLog("Icon saved at: " + filePath, false);
            } else {
                LogManager.getInstance().addLog("No icon found for: " + fileName, true);
            }
        } catch (IOException e) {
            LogManager.getInstance().addLog("Error fetching icon for: " + fileName, true);
        }
    }

    private static void downloadImage(String imageUrl, String filePath) {
        String pngPath = filePath.endsWith(".png") ? filePath : filePath + ".png";
        String icoPath = filePath.replace(".png", "").replace(".jpg", "") + ".ico";

        Task<Void> downloadTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    URL url = new URL(imageUrl);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.connect();

                    int statusCode = httpURLConnection.getResponseCode();

                    if (statusCode == HttpURLConnection.HTTP_FORBIDDEN || statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
                        LogManager.getInstance().addLog("Error " + statusCode + ": Skipping download for URL: " + imageUrl, true);
                        return null;
                    }

                    try (InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                         FileOutputStream fileOutputStream = new FileOutputStream(pngPath)) {

                        byte[] dataBuffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(dataBuffer, 0, 1024)) != -1) {
                            fileOutputStream.write(dataBuffer, 0, bytesRead);
                        }
                    }
                    LogManager.getInstance().addLog("PNG image downloaded successfully: " + pngPath, false);

                    BufferedImage originalImage = ImageIO.read(new File(pngPath));
                    BufferedImage resizedImage = resizeImage(originalImage);

                    try (FileOutputStream icoOutputStream = new FileOutputStream(icoPath)) {
                        ICOEncoder.write(resizedImage, icoOutputStream);
                    }
                    LogManager.getInstance().addLog("ICO image created successfully: " + icoPath, false);

                    Path icoFilePath = Paths.get(icoPath);
                    Files.setAttribute(icoFilePath, "dos:hidden", true);
                    LogManager.getInstance().addLog("ICO image set to hidden: " + icoPath, false);
                } catch (IOException e) {
                    LogManager.getInstance().addLog("Error downloading or converting image: ", true);
                }
                return null;
            }
        };
        new Thread(downloadTask).start();
    }

    private static BufferedImage resizeImage(BufferedImage originalImage) {
        BufferedImage resizedImage = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, 256, 256, null);
        g.dispose();
        return resizedImage;
    }
}