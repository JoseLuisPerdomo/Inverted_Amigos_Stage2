package org.ulpgc.inverted_index.implementations;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DocumentReader {
    // Reads text documents from a specified directory and populates metadata
    public static List<String> readDocumentsFromDirectory(String directory, Map<String, String> bookMetadata) throws IOException {
        List<String> documents = new ArrayList<>(); // List to hold document contents
        File dir = new File(directory);

        // Filter for .txt files in the specified directory
        for (File file : dir.listFiles((d, name) -> name.endsWith(".txt"))) {
            StringBuilder content = new StringBuilder(); // Accumulate file content in a StringBuilder
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                // Read file line by line, adding each line to the content
                while ((line = reader.readLine()) != null) {
                    content.append(line).append(" ");
                }
            }
            documents.add(content.toString()); // Add content to the documents list
            String bookId = file.getName().split("_")[0]; // Extract book ID from the filename
            bookMetadata.put(content.toString(), bookId); // Store the metadata for the document
        }
        return documents; // Return the list of documents
    }
}
