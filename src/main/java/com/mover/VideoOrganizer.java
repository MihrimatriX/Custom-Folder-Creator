package com.mover;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

public class VideoOrganizer {
    public static void execute(String path) {
        try (Stream<Path> paths = Files.list(Paths.get(path))) {
            paths.filter(Files::isRegularFile).forEach(filePath -> {
                String fileName = filePath.getFileName().toString();

                if (fileName.endsWith(".mkv") ||
                        fileName.endsWith(".mp4") ||
                        fileName.endsWith(".avi")) {
                    String videoName = fileName.substring(0, fileName.lastIndexOf('.'));

                    Path videoFolder = Paths.get(path, videoName);
                    try {
                        Files.createDirectories(videoFolder);

                        Path destinationFile = videoFolder.resolve(fileName);
                        Files.move(filePath, destinationFile, StandardCopyOption.REPLACE_EXISTING);

                        String videoDetails = getVideoDetails(destinationFile.toString());
                        Path detailsPath = videoFolder.resolve("Video-Details.txt");
                        Files.write(detailsPath, videoDetails.getBytes());

                        String desktopIniContent = "[.ShellClassInfo]\n" +
                                "IconFile=" + fileName.split("\\.")[0] + ".ico\n" +
                                "IconIndex=0\n" +
                                "ConfirmFileOp=0\n" +
                                "NoSharing=1\n" +
                                "InfoTip=This " + fileName.split("\\.")[0] + " movie\n";
                        Path desktopIniPath = videoFolder.resolve("desktop.ini");

                        try (FileOutputStream fos = new FileOutputStream(desktopIniPath.toFile())) {
                            fos.write(desktopIniContent.getBytes(StandardCharsets.ISO_8859_1));
                        } catch (IOException e) {
                            LogManager.getInstance().addLog("desktop.ini dosyası yazılırken hata oluştu.", true);
                        }
                        setHiddenAndSystemAttributes(desktopIniPath);

                        Process setSystemFolder = Runtime.getRuntime().exec("attrib +s \"" + videoFolder + "\"");
                        setSystemFolder.waitFor();

                        String autorunContent = "[autorun]\n" +
                                "OPEN=C:\\Program Files\\VideoLAN\\VLC\\vlc.exe " + fileName + "\n" +
                                "ICON=" + videoName + ".ico\n";
                        Path autorunPath = videoFolder.resolve("autorun.inf");
                        Files.write(autorunPath, autorunContent.getBytes());

                        String runBatContent = "@echo off\n" +
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
                        Path runBatPath = videoFolder.resolve("run.bat");
                        Files.write(runBatPath, runBatContent.getBytes());
                        LogManager.getInstance().addLog(videoName + " için klasör ve dosyalar oluşturuldu.", false);
                    } catch (IOException | InterruptedException e) {
                        LogManager.getInstance().addLog("Klasör oluşturma hatası. " , true);
                    }
                }
            });
        } catch (IOException e) {
            LogManager.getInstance().addLog("Klasör okuma hatası.", true);
        }
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