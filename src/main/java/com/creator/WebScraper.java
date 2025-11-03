package com.creator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.imageio.ImageIO;
import javax.net.ssl.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;

import static com.creator.HtmlHelper.saveHtmlToFile;
import static com.creator.VideoOrganizer.convertTurkishChars;

public class WebScraper {
    private static SSLSocketFactory sslSocketFactory;

    static {
        // SSL sertifika kontrolünü devre dışı bırak
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            sslSocketFactory = sc.getSocketFactory();
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void fetchIcon(String fileName, String targetDirectory,
            javafx.scene.control.ProgressBar progressBar) {
        updateProgress(progressBar, 0.1, "İkon indirme işlemi başlatıldı: " + fileName);
        String baseUrl = "https://www.google.com";
        String searchUrl;
        try {
            searchUrl = baseUrl + "/search?q=" + convertTurkishChars(fileName)
                    + URLEncoder.encode(" folder icon", "UTF-8") + "&udm=2";
            updateProgress(progressBar, 0.2, "Arama URL'i oluşturuldu: " + searchUrl);
        } catch (UnsupportedEncodingException e) {
            LogManager.getInstance().addLog("URL kodlama hatası: " + e.getMessage(), true);
            return;
        }

        try {
            updateProgress(progressBar, 0.3, "Google araması yapılıyor...");
            Document searchPage = Jsoup.connect(searchUrl)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .ignoreHttpErrors(true)
                    .sslSocketFactory(sslSocketFactory)
                    .get();

            saveHtmlToFile(searchPage.html(), targetDirectory, "Google-Search-Result", "Google Page");
            updateProgress(progressBar, 0.4, "Google sayfası başarıyla kaydedildi.");

            var iconElements = searchPage.select("div[data-attrid='images universal']");
            if (iconElements.size() < 2) {
                LogManager.getInstance().addLog("İkon elementi bulunamadı: " + fileName, true);
                return;
            }

            Element iconElement = iconElements.get(1);
            String deviantPageUrl = iconElement.attr("data-lpage");
            if (deviantPageUrl.isEmpty()) {
                LogManager.getInstance().addLog("Deviant sayfası URL'i bulunamadı: " + fileName, true);
                return;
            }

            updateProgress(progressBar, 0.5, "Deviant sayfası açılıyor: " + deviantPageUrl);
            Document deviantPage = Jsoup.connect(deviantPageUrl)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .ignoreHttpErrors(true)
                    .sslSocketFactory(sslSocketFactory)
                    .get();

            saveHtmlToFile(deviantPage.html(), targetDirectory, "Deviant-Search-Result", "Deviant Page");
            updateProgress(progressBar, 0.6, "Deviant sayfası başarıyla kaydedildi.");

            Element deviantFileIcon = deviantPage.selectFirst("img[property='contentUrl']");
            String deviantFileUrl = deviantFileIcon != null ? deviantFileIcon.attr("src") : "";

            if (!deviantFileUrl.isEmpty()) {
                updateProgress(progressBar, 0.7, "İkon URL'i bulundu: " + deviantFileUrl);
                String sanitizedFileName = convertTurkishChars(fileName);
                String filePath = targetDirectory + "/" + sanitizedFileName + ".png";
                downloadImage(deviantFileUrl, filePath, progressBar);
            } else {
                LogManager.getInstance().addLog("İkon URL'i bulunamadı: " + fileName, true);
            }
        } catch (IOException e) {
            LogManager.getInstance().addLog("Hata oluştu - " + fileName + ": " + e.getMessage(), true);
            e.printStackTrace();
        } catch (Exception e) {
            LogManager.getInstance().addLog("Beklenmeyen hata - " + fileName + ": " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private static void updateProgress(javafx.scene.control.ProgressBar progressBar, double progress, String message) {
        if (progressBar != null) {
            javafx.application.Platform.runLater(() -> {
                progressBar.setProgress(progress);
                LogManager.getInstance().addLog(message, false);
            });
        }
    }

    private static void downloadImage(String imageUrl, String filePath, javafx.scene.control.ProgressBar progressBar) {
        updateProgress(progressBar, 0.8, "İkon indirme başlatıldı: " + imageUrl);
        String sanitizedFilePath = convertTurkishChars(filePath);
        String pngPath = sanitizedFilePath.endsWith(".png") ? sanitizedFilePath : sanitizedFilePath + ".png";
        String icoPath = sanitizedFilePath.replace(".png", "").replace(".jpg", "") + ".ico";

        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(imageUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            httpURLConnection.connect();

            int statusCode = httpURLConnection.getResponseCode();
            LogManager.getInstance().addLog("Bağlantı durumu: " + statusCode, false);

            if (statusCode == HttpURLConnection.HTTP_FORBIDDEN || statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
                LogManager.getInstance().addLog("Hata " + statusCode + ": İndirme atlanıyor - URL: " + imageUrl, true);
                return;
            }

            try (InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                    FileOutputStream fileOutputStream = new FileOutputStream(pngPath)) {

                byte[] dataBuffer = new byte[1024];
                int bytesRead;
                int totalBytesRead = 0;
                while ((bytesRead = inputStream.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }
                LogManager.getInstance().addLog("PNG dosyası indirildi (" + totalBytesRead + " bytes): " + pngPath,
                        false);
            }

            File pngFile = new File(pngPath);
            if (!pngFile.exists() || pngFile.length() == 0) {
                LogManager.getInstance().addLog("PNG dosyası oluşturulamadı veya boş: " + pngPath, true);
                return;
            }

            BufferedImage originalImage = ImageIO.read(pngFile);
            if (originalImage == null) {
                LogManager.getInstance().addLog("PNG dosyası okunamadı: " + pngPath, true);
                return;
            }

            // Create 256x256 icon
            BufferedImage iconImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = iconImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(originalImage, 0, 0, 256, 256, null);
            g.dispose();

            // Save as PNG with .ico extension
            try (FileOutputStream icoOutputStream = new FileOutputStream(icoPath)) {
                ImageIO.write(iconImage, "png", icoOutputStream);
                LogManager.getInstance().addLog("ICO dosyası oluşturuldu: " + icoPath, false);
            }

            try {
                Path icoFilePath = Paths.get(icoPath);
                Files.setAttribute(icoFilePath, "dos:hidden", true);
                LogManager.getInstance().addLog("ICO dosyası gizli yapıldı: " + icoPath, false);
            } catch (IOException e) {
                LogManager.getInstance().addLog("ICO dosyası gizlenemedi: " + e.getMessage(), true);
            }

        } catch (IOException e) {
            LogManager.getInstance().addLog("İndirme/dönüştürme hatası: " + e.getMessage(), true);
            e.printStackTrace();
        } catch (Exception e) {
            LogManager.getInstance().addLog("Beklenmeyen hata: " + e.getMessage(), true);
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }
}