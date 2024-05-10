package io.github.railroad.utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileHandler {
    public static String getExtension(String path) {
        int i = path.lastIndexOf('.');
        if (i > 0) {
            return path.substring(i + 1);
        }

        return null;
    }

    public static void copyUrlToFile(String url, Path path) {
        try (InputStream in = new URI(url).toURL().openStream()) {
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | URISyntaxException exception) {
            throw new RuntimeException("Failed to copy URL to file", exception);
        }
    }

    public static void updateKeyValuePairByLine(String key, String value, Path file) throws IOException {
        var stringBuilder = new StringBuilder();

        List<String> lines = Files.readAllLines(file);
        for (String line : lines) {
            if (line.startsWith(key + "=")) {
                // Replace the existing value
                line = key + "=" + value;
            }

            stringBuilder.append(line).append("\n");
        }

        Files.writeString(file, stringBuilder.toString());
    }

    public static void unzipFile(String fileZip, String dstDir) throws IOException {
        try (var zipInputStream = new ZipInputStream(new FileInputStream(fileZip))) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                Path newFile = newFile(Path.of(dstDir), zipEntry);
                if (zipEntry.isDirectory()) {
                    Files.createDirectories(newFile);
                    if (!Files.isDirectory(newFile))
                        throw new IOException("Failed to create directory " + newFile);
                } else {
                    // fix for Windows-created archives
                    Path parent = newFile.getParent();
                    Files.createDirectories(parent);
                    if (!Files.isDirectory(parent))
                        throw new IOException("Failed to create directory " + parent);

                    // write file content
                    Files.copy(zipInputStream, newFile, StandardCopyOption.REPLACE_EXISTING);
                }

                zipEntry = zipInputStream.getNextEntry();
            }

            zipInputStream.closeEntry();
        }
    }

    public static Path newFile(Path destinationDir, ZipEntry zipEntry) throws IOException {
        var destFile = Paths.get(destinationDir.toString(), zipEntry.getName());

        if (!destFile.normalize().startsWith(destinationDir))
            throw new IOException("Bad zip entry: " + zipEntry.getName());

        return destFile;
    }
}