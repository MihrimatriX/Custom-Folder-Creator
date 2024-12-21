package com.creator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ChromeBookmarksReader {
    public static List<Bookmark> readBookmarks(String comboBoxValue, String folderName) {
        List<Bookmark> bookmarks = new ArrayList<>();
        Path bookmarksFilePath = getBookmarksFilePath(comboBoxValue);

        try (FileReader reader = new FileReader(bookmarksFilePath.toFile())) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject roots = jsonObject.getAsJsonObject("roots");
            JsonObject bookmarkBar = roots.getAsJsonObject("bookmark_bar");
            JsonArray children = bookmarkBar.getAsJsonArray("children");

            JsonArray targetChildren = null;
            for (JsonElement element : children) {
                JsonObject bookmark = element.getAsJsonObject();
                if (folderName.equals(bookmark.get("name").getAsString())) {
                    targetChildren = bookmark.getAsJsonArray("children");
                    break;
                }
            }

            if (targetChildren != null) {
                extractBookmarks(targetChildren, bookmarks);
            } else {
                System.out.println(folderName + " adında bir bookmark klasörü bulunamadı.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bookmarks;
    }

    private static void extractBookmarks(JsonArray children, List<Bookmark> bookmarks) {
        for (var element : children) {
            JsonObject child = element.getAsJsonObject();
            String type = child.get("type").getAsString();

            if ("url".equals(type)) {
                String name = child.get("name").getAsString();
                String url = child.get("url").getAsString();
                bookmarks.add(new Bookmark(name, url));
            } else if ("folder".equals(type)) {
                JsonArray nestedChildren = child.getAsJsonArray("children");
                extractBookmarks(nestedChildren, bookmarks);
            }
        }
    }

    static Path getBookmarksFilePath(String comboBoxValue) {
        String home = System.getProperty("user.home");
        return switch (comboBoxValue.toLowerCase()) {
            case "chrome" -> Path.of(home, "AppData", "Local", "Google", "Chrome", "User Data", "Default", "Bookmarks");
            case "edge" -> Path.of(home, "AppData", "Local", "Microsoft", "Edge", "User Data", "Default", "Bookmarks");
            case "firefox" -> Path.of(home, "Firefox");
            case "safari" -> Path.of(home, "Safari");
            default -> throw new IllegalStateException("Unexpected value: " + comboBoxValue.toLowerCase());
        };
    }

    public record Bookmark(String name, String url) {
        @Override
        public String toString() {
            return "Bookmark{" +
                    "name='" + name + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }
}