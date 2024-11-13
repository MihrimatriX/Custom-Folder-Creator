package com.mover;

import java.io.File;
import java.io.IOException;
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
                        Path detailsPath = videoFolder.resolve("video_details.txt");
                        Files.write(detailsPath, videoDetails.getBytes());

                        String desktopIniContent = "[.ShellClassInfo]\n" +
                                "IconFile=" + fileName.split("\\.")[0] + ".ico\n" +
                                "IconIndex=0\n" +
                                "ConfirmFileOp=0\n" +
                                "NoSharing=1\n" +
                                "InfoTip=This " + fileName.split("\\.")[0] + " movie\n";
                        Path desktopIniPath = videoFolder.resolve("desktop.ini");
                        Files.write(desktopIniPath, desktopIniContent.getBytes());
                        setHiddenAndSystemAttributes(desktopIniPath);

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
                    } catch (IOException e) {
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

                "Çözünürlük: " + mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "Width") + "x" + mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "Height") + "\n" +
                "Video Codec: " + mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "Video_Codec_List") + "\n" +
                ": " + mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "") + "\n" +
                "Video Bit Hızı: " + mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "BitRate/String") + "\n" +
                "Kare Hızı: " + mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "FrameRate/String") + "\n" +
                "Görüntü En-Boy Oranı: " + mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "DisplayAspectRatio/String") + "\n" +

                "Ses Codec: " + mediaInfo.Get(MediaInfo.StreamKind.Audio, 0, "CodecID") + "\n" +
                "Ses Kanalları: " + mediaInfo.Get(MediaInfo.StreamKind.Audio, 0, "Channel(s)") + "\n" +
                "Ses Bit Hızı: " + mediaInfo.Get(MediaInfo.StreamKind.Audio, 0, "BitRate/String") + "\n" +
                "Ses Örnekleme Hızı: " + mediaInfo.Get(MediaInfo.StreamKind.Audio, 0, "SamplingRate/String") + "\n" +
                "Ses Dili: " + mediaInfo.Get(MediaInfo.StreamKind.Audio, 0, "Language/String") + "\n" +

                "Altyazı Dili: " + mediaInfo.Get(MediaInfo.StreamKind.Text, 0, "Language/String") + "\n";

        mediaInfo.Close();
        return details;
    }

    private static void addStreamInfo(MediaInfo mediaInfo) {
        addStreamInfo(mediaInfo, MediaInfo.StreamKind.General, 0);
    }

    private static void addStreamInfo(MediaInfo mediaInfo, MediaInfo.StreamKind streamKind, int streamIndex) {
        int paramCount = mediaInfo.Count_Get(streamKind, streamIndex);
        for (int i = 0; i < paramCount; i++) {
            String paramName = mediaInfo.Get(streamKind, streamIndex, i, MediaInfo.InfoKind.Name);
            String paramValue = mediaInfo.Get(streamKind, streamIndex, i, MediaInfo.InfoKind.Text);

            if (paramName != null && paramValue != null && !paramValue.equalsIgnoreCase("N/A") && !paramValue.isEmpty()) {
                System.out.println(paramName + " : " + paramValue);
            }
        }
    }

    static void displayVideoDetailsPrint(File file) {
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.Open(file.getAbsolutePath());

        addStreamInfo(mediaInfo);

        int videoTrackCount = mediaInfo.Count_Get(MediaInfo.StreamKind.Video);
        for (int i = 0; i < videoTrackCount; i++) {
            addStreamInfo(mediaInfo, MediaInfo.StreamKind.Video, i);
        }

        int audioTrackCount = mediaInfo.Count_Get(MediaInfo.StreamKind.Audio);
        for (int i = 0; i < audioTrackCount; i++) {
            addStreamInfo(mediaInfo, MediaInfo.StreamKind.Audio, i);
        }

        int textTrackCount = mediaInfo.Count_Get(MediaInfo.StreamKind.Text);
        for (int i = 0; i < textTrackCount; i++) {
            addStreamInfo(mediaInfo, MediaInfo.StreamKind.Text, i);
        }
        mediaInfo.Close();
    }

    private static void setHiddenAndSystemAttributes(Path path) {
        try {
            Process process = Runtime.getRuntime().exec("attrib +h +s \"" + path.toString() + "\"");
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            LogManager.getInstance().addLog("Hata oluştu (attriubte ayarlama)", true);
        }
    }
}
