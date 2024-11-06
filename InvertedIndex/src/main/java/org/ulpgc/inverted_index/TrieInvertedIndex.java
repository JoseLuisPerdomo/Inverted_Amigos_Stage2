package org.ulpgc.inverted_index;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

// Main class for the Inverted Index program
public class TrieInvertedIndex {
    private Trie trie;  // Trie data structure for indexing words
    private Map<String, String> bookMetadata;  // Map to hold book metadata (file content to book ID)

    // Constructor initializes the trie and metadata map
    public TrieInvertedIndex() {
        this.trie = new Trie();
        this.bookMetadata = new HashMap<>();
    }

    // Method to index books in the specified directory
    public void indexBooks(String directory) throws IOException {
        // Read documents and populate metadata
        List<String> documents = DocumentReader.readDocumentsFromDirectory(directory, bookMetadata);
        for (String document : documents) {
            String[] words = preprocessText(document); // Preprocess text to extract words
            String bookId = bookMetadata.get(document); // Get the book ID from the metadata
            for (int position = 0; position < words.length; position++) {
                trie.insert(words[position], bookId, position); // Insert each word with its position into the trie
            }
        }
        saveTrieAsMessagePack("trie_index_by_prefix"); // Save the trie structure to MessagePack files
    }

    // Method to search for a word in the inverted index
    public Map<String, List<Integer>> searchWord(String word) {
        return trie.search(word); // Return the search results from the trie
    }

    // Preprocesses text by converting to lowercase and splitting into words
    private String[] preprocessText(String text) {
        return Arrays.stream(text.toLowerCase().split("\\W+"))
                .filter(word -> !word.isEmpty())
                .toArray(String[]::new);
    }

    // Saves the trie as MessagePack files in the specified output directory
    private void saveTrieAsMessagePack(String outputDirectory) throws IOException {
        File dir = new File(outputDirectory);
        if (!dir.exists()) {
            dir.mkdirs(); // Create directory if it doesn't exist
        }
        for (Character prefix : trie.getRoot().children.keySet()) {
            File file = new File(dir, prefix + ".msgpack"); // Create a file for each prefix
            try (FileOutputStream fos = new FileOutputStream(file);
                 MessagePacker packer = MessagePack.newDefaultPacker(fos)) {
                packer.packMapHeader(1); // Start with a root map
                packer.packString(String.valueOf(prefix)); // Pack the prefix as a string
                trie.getRoot().children.get(prefix).toMessagePack(packer); // Serialize the corresponding trie node
            }
        }
    }

    // Main method to run the indexing and searching
    public static void main(String[] args) {
        try {
            TrieInvertedIndex invertedIndex = new TrieInvertedIndex(); // Create an instance of InvertedIndex
            String directory = "C:\\University\\Big Data\\gutenberg_books"; // Specify the directory of books
            invertedIndex.indexBooks(directory); // Index the books in the specified directory
            System.out.println("Indexing completed.");

            // Test the search functionality with a sample word
            String searchWord = "julia";
            Map<String, List<Integer>> results = invertedIndex.searchWord(searchWord);
            if (results != null && !results.isEmpty()) {
                System.out.println("Search results for '" + searchWord + "':");
                for (Map.Entry<String, List<Integer>> entry : results.entrySet()) {
                    System.out.println("Book ID: " + entry.getKey() + ", Positions: " + entry.getValue());
                }
            } else {
                System.out.println("No results found for '" + searchWord + "'.");
            }
        } catch (IOException e) {
            System.err.println("Error during indexing: " + e.getMessage());
        }
    }
}
