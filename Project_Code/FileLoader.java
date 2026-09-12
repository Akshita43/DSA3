import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * FileLoader.java
 * ------------------------------------------------------------
 * Reads the DICTIONARY (data/dictionary.txt) and CATALOG
 * (data/catalog.txt) text files from disk instead of hardcoding
 * them inside the Java source. Lines starting with '#' or blank
 * lines are skipped.
 * ------------------------------------------------------------
 */
public class FileLoader {

    /** Reads a plain-text list file (one entry per line, '#' = comment). */
    public static List<String> loadLines(String path) {
        List<String> result = new ArrayList<>();
        List<String> candidates = Arrays.asList(
                path,
                "data/" + path,
                "../data/" + path,
                "./" + path
        );

        for (String candidate : candidates) {
            File f = new File(candidate);
            if (f.exists()) {
                try (BufferedReader br = Files.newBufferedReader(f.toPath())) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String trimmed = line.trim();
                        if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                        result.add(trimmed.toLowerCase());
                    }
                    return result;
                } catch (IOException e) {
                    System.out.println("Warning: could not read " + candidate + " (" + e.getMessage() + ")");
                }
            }
        }
        System.out.println("Warning: file \"" + path + "\" not found in expected locations. " +
                "Make sure the 'data' folder sits next to where you run the program from.");
        return result;
    }

    public static List<String> loadDictionary() {
        return loadLines("dictionary.txt");
    }

    public static List<String> loadCatalog() {
        return loadLines("catalog.txt");
    }
}
