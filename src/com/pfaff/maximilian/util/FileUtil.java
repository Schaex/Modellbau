package com.pfaff.maximilian.util;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class FileUtil {
    private FileUtil() {}

    /**
     * Splits a string at the last occurrence of a dot. Example <br>
     * "image.png" becomes ["image", ".png"]
     * @param fileName The file name.
     * @return An array of one or two strings, depending on whether the name had an extension or not.
     */
    public static String[] splitFileNameAtExtension(String fileName) {
        final int extensionIndex = fileName.lastIndexOf('.');

        if (extensionIndex == -1) {
            // No extension
            return new String[]{fileName};
        } else {
            final String pre = fileName.substring(0, extensionIndex);
            final String ext = fileName.substring(extensionIndex);

            return new String[]{pre, ext};
        }
    }

    /**
     * Converts a given name string to a Path and resolves it against the parent Path to create a valid and new file path.
     * If necessary, it will add a suffix until the path does not exist yet.
     * @param parent Parent directory.
     * @param name Name of the file or directory that should be a child of the parent.
     * @return A unique file path.
     */
    public static Path resolveUniqueFilePath(Path parent, String name) {
        Path path = parent.resolve(name);

        if (Files.notExists(path)) {
            return path;
        }

        final String[] parts = splitFileNameAtExtension(name);

        int numbering = 2;

        if (parts.length == 1) {
            do {
                path = parent.resolve(name + " (" + numbering++ + ")");
            } while (Files.exists(path));
        } else {
            final String rawName = parts[0];
            final String extension = parts[1];

            do {
                path = parent.resolve(rawName + " (" + numbering++ + ")" + extension);
            } while (Files.exists(path));
        }

        return path;
    }

    /**
     * Iterates over the list and writes each string to a file.
     * @param file The file that is being written to.
     * @param lines A list of strings containing the lines of the file, without line breaks.
     */
    public static void dumpToFile(File file, List<String> lines) throws IOException {
        final char[] lineSep = System.lineSeparator().toCharArray();

        try (FileWriter writer = new FileWriter(file)) {
            for (String line : lines) {
                writer.write(line);
                writer.write(lineSep);
            }
        }
    }

    /**
     * Displays a file open dialog and lets the user choose a directory.
     * @return The directory that the user selected, or {@code null} if they closed the window.
     */
    public static File openDir() {
        JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        return chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile() : null;
    }
}

