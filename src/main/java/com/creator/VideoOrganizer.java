package com.creator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class VideoOrganizer {
    public static String convertTurkishChars(String input) {
        Map<Character, Character> turkishCharMap = new HashMap<>();
        turkishCharMap.put('ç', 'c');
        turkishCharMap.put('Ç', 'C');
        turkishCharMap.put('ğ', 'g');
        turkishCharMap.put('Ğ', 'G');
        turkishCharMap.put('ı', 'i');
        turkishCharMap.put('İ', 'I');
        turkishCharMap.put('ö', 'o');
        turkishCharMap.put('Ö', 'O');
        turkishCharMap.put('ş', 's');
        turkishCharMap.put('Ş', 'S');
        turkishCharMap.put('ü', 'u');
        turkishCharMap.put('Ü', 'U');

        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            result.append(turkishCharMap.getOrDefault(c, c));
        }

        return result.toString();
    }

    public static void execute(String path) {
        try (Stream<Path> paths = Files.list(Paths.get(normalizePath(path.trim())))) {
            paths.forEach(filePath -> {
                if (Files.isDirectory(filePath)) {
                    processFolderOrFile(filePath);
                } else if (Files.isRegularFile(filePath)) {
                    processFolderOrFile(filePath);
                }
            });
        } catch (IOException e) {
            LogManager.getInstance().addLog("Klasör veya dosya okuma hatası.", true);
        }
    }

    private static void processFolderOrFile(Path path) {
        String name = normalizePath(path.getFileName().toString().trim());
        Path targetFolder;

        if (Files.isRegularFile(path)) {
            String fileBaseName = name;

            int dotIndex = name.lastIndexOf('.');
            if (dotIndex > 0) {
                fileBaseName = name.substring(0, dotIndex).trim();
            }

            targetFolder = path.getParent().resolve(fileBaseName);
        } else if (Files.isDirectory(path)) {
            targetFolder = path;
        } else {
            LogManager.getInstance().addLog("Bilinmeyen türde bir öğe atlandı: " + name, true);
            return;
        }

        try {
            Files.createDirectories(targetFolder);

            if (Files.isRegularFile(path)) {
                Path destinationFile = targetFolder.resolve(name);
                Files.move(path, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                path = destinationFile;
            }

            String videoDetails = getVideoDetails(path.toString());
            Path detailsPath = targetFolder.resolve("Video-Details.txt");
            Files.write(detailsPath, videoDetails.getBytes());

            String icoFileName = convertTurkishChars(name.replaceAll("\\.\\w+$", "")) + ".ico";
            String desktopIniContent = "[.ShellClassInfo]\n" +
                    "IconResource=" + icoFileName + ",0\n" +
                    "[ViewState]\n" +
                    "Mode=\n" +
                    "Vid=\n" +
                    "FolderType=Videos\n";

            Path desktopIniPath = targetFolder.resolve("desktop.ini");
            
            // Önce klasörü sistem klasörü yap
            Process setSystemFolder = Runtime.getRuntime().exec("attrib +s \"" + targetFolder + "\"");
            setSystemFolder.waitFor();

            // desktop.ini dosyasını oluştur
            try (FileOutputStream fos = new FileOutputStream(desktopIniPath.toFile())) {
                fos.write(desktopIniContent.getBytes(StandardCharsets.UTF_8));
            }

            // desktop.ini dosyasını gizli ve sistem dosyası yap
            Process setAttributes = Runtime.getRuntime().exec("attrib +s +h \"" + desktopIniPath + "\"");
            setAttributes.waitFor();

            // Klasör özniteliklerini güncelle
            Process updateFolder = Runtime.getRuntime().exec(new String[] {
                "cmd", "/c",
                "attrib +r \"" + targetFolder + "\" && " +
                "attrib +s +h \"" + desktopIniPath + "\" && " +
                "attrib +s \"" + targetFolder + "\""
            });
            updateFolder.waitFor();

            // ICO dosyasını sistem dosyası yap
            Path icoPath = targetFolder.resolve(icoFileName);
            if (Files.exists(icoPath)) {
                Process setIcoAttributes = Runtime.getRuntime().exec("attrib +s \"" + icoPath + "\"");
                setIcoAttributes.waitFor();
            }

            String autorunContent = "[autorun]\n" +
                    "OPEN=C:\\Program Files\\VideoLAN\\VLC\\vlc.exe " + name + "\n" +
                    "ICON=" + name + ".ico\n";
            Path autorunPath = targetFolder.resolve("autorun.inf");
            Files.write(autorunPath, autorunContent.getBytes());

            String runBatContent = generateRunBatContent(name);
            Path runBatPath = targetFolder.resolve("run.bat");
            Files.write(runBatPath, runBatContent.getBytes());

            LogManager.getInstance().addLog(name + " için klasör ve dosyalar oluşturuldu.", false);
        } catch (IOException e) {
            LogManager.getInstance().addLog("Klasör veya dosya işleme hatası: " + name, true);
        } catch (InterruptedException e) {
            LogManager.getInstance().addLog("İşlem kesintiye uğradı: " + name, true);
            Thread.currentThread().interrupt(); // Kesinti durumunu yeniden ayarla
        }
    }

    private static String generateRunBatContent(String fileName) {
        return "@echo off\n" +
                "REM VLC Player kontrol et\n" +
                "if exist \"%ProgramFiles%\\VideoLAN\\VLC\\vlc.exe\" (\n" +
                "    echo VLC Player bulundu. VLC ile oynatiliyor...\n" +
                "    \"%ProgramFiles%\\VideoLAN\\VLC\\vlc.exe\" \"" + fileName + "\"\n" +
                ") else (\n" +
                "    REM Eğer 32-bit VLC kuruluysa\n" +
                "    if exist \"%ProgramFiles(x86)%\\VideoLAN\\VLC\\vlc.exe\" (\n" +
                "        echo 32-bit VLC Player bulundu. VLC ile oynatiliyor...\n" +
                "        \"%ProgramFiles(x86)%\\VideoLAN\\VLC\\vlc.exe\" \"" + fileName + "\"\n" +
                "    ) else (\n" +
                "        REM VLC yoksa Splash Player ile çalıştır\n" +
                "        if exist \"%ProgramFiles%\\Mirillis\\Splash\\splash.exe\" (\n" +
                "            echo VLC bulunamadı. Splash Player ile oynatiliyor...\n" +
                "            \"%ProgramFiles%\\Mirillis\\Splash\\splash.exe\" \"" + fileName + "\"\n" +
                "        ) else (\n" +
                "            REM Eğer 32-bit Splash kuruluysa\n" +
                "            if exist \"%ProgramFiles(x86)%\\Mirillis\\Splash\\splash.exe\" (\n" +
                "                echo VLC bulunamadı. Splash Player ile oynatiliyor...\n" +
                "                \"%ProgramFiles(x86)%\\Mirillis\\Splash\\splash.exe\" \"" + fileName + "\"\n" +
                "            ) else (\n" +
                "                echo VLC veya Splash Player bulunamadi. Lütfen bir medya oynatici yükleyin.\n" +
                "            )\n" +
                "        )\n" +
                "    )\n" +
                ")\n";
    }

    private static String normalizePath(String path) {
        return path.replaceAll("[^\\x20-\\x7E]", "").trim();
    }

    private static String getVideoDetails(String filePath) {
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.Open(filePath);

        String details = "Dosya: " + filePath + "\n" +
                "Dosya Boyutu: " + mediaInfo.Get(MediaInfo.StreamKind.General, 0, "FileSize/String") + "\n" +
                "Süre: " + mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Duration/String") + "\n" +
                "Format: " + mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Format") + "\n" +
                "Toplam Bit Hızı: " + mediaInfo.Get(MediaInfo.StreamKind.General, 0, "OverallBitRate/String") + "\n" +
                "Başlık: " + mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Title") + "\n" +
                "Sanatçı: " + mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Performer") + "\n" +
                "Albüm: " + mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Album") + "\n" +
                "Tür: " + mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Genre") + "\n" +
                "Yayıncı: " + mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Publisher") + "\n" +
                "Kaydedilme Tarihi: " + mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Recorded_Date") + "\n" +
                "Yazım Programı: " + mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Encoded_Application") + "\n" +
                "Yazım Kütüphanesi: " + mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Encoded_Library") + "\n" +
                "Açıklama: " + mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Description") + "\n" +
                "Telif Hakkı: " + mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Copyright") + "\n" +
                "Anahtar Kelimeler: " + mediaInfo.Get(MediaInfo.StreamKind.General, 0, "Keywords") + "\n" +

                "Çözünürlük: " + mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "Width") + " x " + mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "Height") + "\n" +
                "Video Codec: " + mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "CodecID") + "\n" +
                "Video Bit Hızı: " + mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "BitRate/String") + "\n" +
                "Kare Hızı: " + mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "FrameRate/String") + "\n" +
                "En-Boy Oranı: " + mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "DisplayAspectRatio/String") + "\n" +
                "Tarama Türü: " + mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "ScanType") + "\n" +
                "Renk Alanı: " + mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "ColorSpace") + "\n" +
                "Renk Örnekleme: " + mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "ChromaSubsampling") + "\n" +
                "HDR Formatı: " + mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "HDR_Format") + "\n" +
                "Format Profili: " + mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "Format_Profile") + "\n" +

                "Ses Codec: " + mediaInfo.Get(MediaInfo.StreamKind.Audio, 0, "CodecID") + "\n" +
                "Ses Formatı: " + mediaInfo.Get(MediaInfo.StreamKind.Audio, 0, "Format") + "\n" +
                "Kanallar: " + mediaInfo.Get(MediaInfo.StreamKind.Audio, 0, "Channel(s)") + "\n" +
                "Kanal Düzeni: " + mediaInfo.Get(MediaInfo.StreamKind.Audio, 0, "ChannelLayout") + "\n" +
                "Örnekleme Hızı: " + mediaInfo.Get(MediaInfo.StreamKind.Audio, 0, "SamplingRate/String") + "\n" +
                "Bit Derinliği: " + mediaInfo.Get(MediaInfo.StreamKind.Audio, 0, "BitDepth/String") + "\n" +
                "Ses Bit Hızı: " + mediaInfo.Get(MediaInfo.StreamKind.Audio, 0, "BitRate/String") + "\n" +
                "Ses Dili: " + mediaInfo.Get(MediaInfo.StreamKind.Audio, 0, "Language/String") + "\n" +

                "Altyazı Formatı: " + mediaInfo.Get(MediaInfo.StreamKind.Text, 0, "Format") + "\n" +
                "Altyazı Codec: " + mediaInfo.Get(MediaInfo.StreamKind.Text, 0, "CodecID") + "\n" +
                "Altyazı Dili: " + mediaInfo.Get(MediaInfo.StreamKind.Text, 0, "Language/String") + "\n" +
                "Altyazı Kodlama: " + mediaInfo.Get(MediaInfo.StreamKind.Text, 0, "Encoding") + "\n" +
                "Gecikme: " + mediaInfo.Get(MediaInfo.StreamKind.Text, 0, "Delay/String") + "\n" +
                "Altyazı Boyutu: " + mediaInfo.Get(MediaInfo.StreamKind.Text, 0, "StreamSize/String") + "\n";
        mediaInfo.Close();
        return details;
    }

    private static void setHiddenAndSystemAttributes(Path path) {
        try {
            Process process = Runtime.getRuntime().exec("attrib +h +s \"" + path.toString() + "\"");
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            LogManager.getInstance().addLog("Error Handle -> (Set Attribute)", true);
        }
    }
}