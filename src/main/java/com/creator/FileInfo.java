package com.creator;

public class FileInfo {
    private final String name;
    private final String extension;
    private final long size;
    private final String path;

    public FileInfo(String name, String extension, long size, String path) {
        this.name = name;
        this.extension = extension;
        this.size = size;
        this.path = path;
    }

    public String getNameWithoutExtension() {
        int dotIndex = name.lastIndexOf(".");
        return (dotIndex == -1) ? name : name.substring(0, dotIndex);
    }

    public String getSizeFormatted() {
        double sizeInGB = size / (1024.0 * 1024.0 * 1024.0);
        if (sizeInGB >= 1) {
            return String.format("%.2f GB", sizeInGB);
        } else {
            double sizeInMB = size / (1024.0 * 1024.0);
            return String.format("%.2f MB", sizeInMB);
        }
    }

    public String getPath() {
        return path;
    }

    public String getExtension() {
        return extension;
    }
}
