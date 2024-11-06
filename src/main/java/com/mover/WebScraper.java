package com.mover;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    static int say = 0;
    public static void fetchIcon(String fileName, String targetDirectory, ImageView iconImageView, Label fileNameLabel) {
        String baseUrl = "https://www.google.com";
        String searchUrl = baseUrl + "/search?q=" + fileName + URLEncoder.encode(" folder icon") + "&udm=2";

        try {
            Document searchPage = Jsoup.connect(searchUrl).get();
            //saveHtmlToFile(searchPage.html(), targetDirectory);

            Element iconElement = searchPage.select("div[data-attrid='images universal']").get(1);
            String deviantPageUrl = iconElement.attr("data-lpage");

            Document deviantPage = Jsoup.connect(deviantPageUrl).get();
            //saveHtmlToFile(deviantPage.html(), targetDirectory);
            Element deviantFileIcon = deviantPage.selectFirst("img[property='contentUrl']");
            String deviantFileUrl = deviantFileIcon != null ? deviantFileIcon.attr("src") : "";

            if (!deviantFileUrl.isEmpty()) {
                String filePath = targetDirectory + "/" + fileName + ".png";
                downloadImage(deviantFileUrl, filePath, iconImageView, fileNameLabel);
                System.out.println("Icon saved at: " + filePath);
            } else {
                System.out.println("No icon found for: " + fileName);
            }
        } catch (IOException e) {
            System.err.println("Error fetching icon for: " + fileName);
            e.printStackTrace();
        }
    }

    private static void downloadImage(String imageUrl, String filePath, ImageView iconImageView, Label fileNameLabel) {
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
                        System.out.println("Error " + statusCode + ": Skipping download for URL: " + imageUrl);
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
                    System.out.println("PNG image downloaded successfully: " + pngPath);

                    BufferedImage originalImage = ImageIO.read(new File(pngPath));
                    BufferedImage resizedImage = resizeImage(originalImage);

                    try (FileOutputStream icoOutputStream = new FileOutputStream(icoPath)) {
                        ICOEncoder.write(resizedImage, icoOutputStream);
                    }
                    System.out.println("ICO image created successfully: " + icoPath);

                    Platform.runLater(() -> {
                        try {
                            FileInputStream fileInputStream = new FileInputStream(pngPath);
                            Image image = new Image(fileInputStream);
                            iconImageView.setImage(image);
                            fileNameLabel.setText("İndirilen İkon: " + icoPath);
                        } catch (FileNotFoundException e) {
                            System.err.println("Error: Resim dosyası bulunamadı " + e.getMessage());
                        }
                    });

                    Path icoFilePath = Paths.get(icoPath);
                    Files.setAttribute(icoFilePath, "dos:hidden", true);
                    System.out.println("ICO image set to hidden: " + icoPath);
                } catch (IOException e) {
                    System.err.println("Error downloading or converting image: " + e.getMessage());
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