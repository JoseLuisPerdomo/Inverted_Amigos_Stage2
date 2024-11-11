package org.ulpgc.query_engine;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchEngine {

    private List<Map<String, String>> metadata;
    private static final String PATH_TO_METADATA = "data/metadata.txt";
    private static final String PATH_TO_HASHED_INDEX = "indexes/hashed/datamart.dat";
    private static final String PATH_TO_DIRECTORY_INDEX = "indexes/directory";
    private static final String PATH_TO_TRIE_DIRECTORY_INDEX = "indexes/trie_directory";
    private static final String PATH_TO_BOOKS_CONTENT_DIRECTORY = "data/books_content";
    private static final String TRIE_END_OF_WORD_FILENAME = "-.txt";

    public enum Field {
        ID("ID"),
        TITLE("Title"),
        AUTHOR("Author"),
        RELEASE_DATE("Release Date"),
        LANGUAGE("Language");

        private final String value;

        Field(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public ResponseList<List<Integer>> searchForBooksWithWord(String word) {
        return searchInHashedIndex(word);
    }

    public ResponseList<List<Integer>> searchForBooksWithWord(String word, String indexer) {
        ResponseList<List<Integer>> list = new ResponseList<List<Integer>>();
        if(Objects.equals(indexer, "hashed"))
            list = searchInHashedIndex(word);
        else if (Objects.equals(indexer, "directory")) {
            list = searchInDirectoryIndex(word);
        } else if (Objects.equals(indexer, "trie")) {
            list = searchInTrieDirectoryIndex(word);
        }
        // @TODO maybe add some default value or raise an error
        return list;
    }

    public ResponseList<List<List<Integer>>> searchForBooksWithMultipleWords(String[] words, String indexer) {
        List<ResponseList<List<Integer>>> accumulateList = new ArrayList<ResponseList<List<Integer>>>();
        for(String word : words) {
            ResponseList<List<Integer>> partialList = searchForBooksWithWord(word, indexer);
            accumulateList.add(partialList);
        }
        return compileResultsForManyWords(accumulateList);
    }

    public ResponseList<List<List<Integer>>> searchForMultiplewithCriteria(String indexer, String[] words, String title, String author, String date, String language) {
        ResponseList<List<List<Integer>>> initialResults = searchForBooksWithMultipleWords(words, indexer);
        if (title != null) {
            initialResults = filterWithMetadata(initialResults, Field.TITLE, title);
        }
        if (author != null) {
            initialResults = filterWithMetadata(initialResults, Field.AUTHOR, author);
        }
        if (date != null) {
            initialResults = filterWithMetadata(initialResults, Field.RELEASE_DATE, date);
        }
        if (language != null) {
            initialResults = filterWithMetadata(initialResults, Field.LANGUAGE, language);
        }
        return initialResults;
    }

    private ResponseList<List<List<Integer>>> compileResultsForManyWords(List<ResponseList<List<Integer>>> results) {
        ResponseList<List<List<Integer>>> compiledResults = new ResponseList<>();
        int differentWordsNumber = results.size();

        // Get the first list of results as the reference list
        List<Map.Entry<Integer, List<Integer>>> firstResult = results.get(0).getResults();

        // Iterate over the first term's result entries (bookId and positions)
        for (Map.Entry<Integer, List<Integer>> book1 : firstResult) {
            int book1Id = book1.getKey();
            List<List<Integer>> combinedPositions = new ArrayList<>();
            boolean isInAllResults = true;

            // Add positions from the first result to combinedPositions
            combinedPositions.add(new ArrayList<>(book1.getValue()));

            // Check for this bookId in the rest of the results
            for (int i = 1; i < differentWordsNumber; i++) {
                List<Map.Entry<Integer, List<Integer>>> wordResults = results.get(i).getResults();
                boolean foundMatch = false;

                // Search for book1Id in the current word's results
                for (Map.Entry<Integer, List<Integer>> book2 : wordResults) {
                    if (book2.getKey().equals(book1Id)) {
                        combinedPositions.add(new ArrayList<>(book2.getValue()));
                        foundMatch = true;
                        break;
                    }
                }

                // If book1Id is not found in one of the lists, stop processing this bookId
                if (!foundMatch) {
                    isInAllResults = false;
                    break;
                }
            }

            // Only add book1Id if it is present in all ResultLists
            if (isInAllResults) {
                Map.Entry<Integer, List<List<Integer>>> compiledEntry = new AbstractMap.SimpleEntry<>(book1Id, combinedPositions);
                compiledResults.addResult(compiledEntry);
            }
        }

        return compiledResults;
    }


    public ResponseList<List<Integer>> searchWithCriteria(String indexer, String word, String title, String author, String date, String language) {
        ResponseList<List<Integer>> initialResults = searchForBooksWithWord(word, indexer);

        if (title != null) {
            initialResults = filterWithMetadata(initialResults, Field.TITLE, title);
        }
        if (author != null) {
            initialResults = filterWithMetadata(initialResults, Field.AUTHOR, author);
        }
        if (date != null) {
            initialResults = filterWithMetadata(initialResults, Field.RELEASE_DATE, date);
        }
        if (language != null) {
            initialResults = filterWithMetadata(initialResults, Field.LANGUAGE, language);
        }

        return initialResults;
    }

    private ResponseList<List<Integer>> searchInHashedIndex(String word) {
        File fileForWord = new File(System.getProperty("user.dir"), PATH_TO_HASHED_INDEX);
        ResponseList<List<Integer>> response = new ResponseList<List<Integer>>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileForWord))) {
            response = ((Map<String, ResponseList<List<Integer>>>) ois.readObject()).get(word);
        } catch (IOException e) {
            System.err.println("Error reading hashed index file: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found during deserialization: " + e.getMessage());
        }
        // @TODO: test this when the file will be ready
        return response;
    }

    private ResponseList<List<Integer>> searchInTrieIndex(String word) {
        // @TODO: write a function for the trie index with msgpack files
        return new ResponseList<>();
    }

    private ResponseList<List<Integer>> searchInDirectoryIndex(String word) {
        String pathToFileForWord = PATH_TO_DIRECTORY_INDEX + "/" + word.toLowerCase() + ".txt";
        File fileForWord = new File(System.getProperty("user.dir"), pathToFileForWord);
        return parseFileForWord(fileForWord);
    }

    private ResponseList<List<Integer>> searchInTrieDirectoryIndex(String word) {
        String pathToFileForWord = String.join("/",
                PATH_TO_TRIE_DIRECTORY_INDEX,
                String.join("/", word.toLowerCase().split("")),
                TRIE_END_OF_WORD_FILENAME
        );
        File fileForWord = new File(System.getProperty("user.dir"), pathToFileForWord);
        return parseFileForWord(fileForWord);
    }

    private ResponseList<List<Integer>> parseFileForWord(File file) {
        // parses into ResponseList files of format 100: 12, 13, 14 ...
        ResponseList<List<Integer>> responseList = new ResponseList<List<Integer>>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    Integer bookId = Integer.parseInt(parts[0].trim());
                    String positionStr = parts[1].trim();

                    List<Integer> positions = parsePositions(positionStr);

                    Map.Entry<Integer, List<Integer>> entry = new AbstractMap.SimpleEntry<>(bookId, positions);
                    responseList.addResult(entry);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading directory index file: " + e.getMessage());
        }
        return responseList;
    }

    private List<Integer> parsePositions(String positionsStr) {
        List<Integer> positions = new ArrayList<>();
        String[] positionsArray = positionsStr.split(",");
        for (String position : positionsArray) {
            positions.add(Integer.parseInt(position.trim()));
        }
        return positions;
    }

    private ResponseList filterWithMetadata(ResponseList results, Field field, String value) {
        // Load metadata if it hasn't been loaded already
        if (metadata == null || metadata.isEmpty()) {
            File metadataFile = new File(System.getProperty("user.dir"), PATH_TO_METADATA);
            loadMetadataFromFile(metadataFile);
        }

        ResponseList filteredResults = new ResponseList();
        String targetField = field.getValue();

        for (Object obj : results.getResults()) {
            Map.Entry<Integer, ?> result = (Map.Entry<Integer, ?>) obj;

            Integer bookId = result.getKey();
            Object positions = result.getValue(); // Could be List<Integer> or List<List<Integer>>

            // Find corresponding metadata entry for the current bookId
            for (Map<String, String> book : metadata) {
                String bookIdString = book.get("ID");

                if (bookIdString == null || bookIdString.isEmpty()) {
                    System.err.println("Metadata entry with missing or empty ID.");
                    continue; // Skip to the next book in metadata if ID is missing
                }

                try {
                    Integer metadataBookId = Integer.parseInt(bookIdString);

                    // Check if book ID in metadata matches the result book ID
                    if (metadataBookId.equals(bookId)) {
                        String fieldValue = book.get(targetField);

                        // Check if the target field value contains the search string
                        if (fieldValue != null && fieldValue.toLowerCase().contains(value.toLowerCase())) {
                            // Handle different types of `positions` value
                            if (positions instanceof List) {
                                if (((List<?>) positions).isEmpty() || ((List<?>) positions).get(0) instanceof Integer) {
                                    // It's a List<Integer>
                                    Map.Entry<Integer, List<Integer>> filteredEntry =
                                            new AbstractMap.SimpleEntry<>(bookId, (List<Integer>) positions);
                                    filteredResults.addResult(filteredEntry);
                                } else {
                                    // It's a List<List<Integer>>
                                    Map.Entry<Integer, List<List<Integer>>> filteredEntry =
                                            new AbstractMap.SimpleEntry<>(bookId, (List<List<Integer>>) positions);
                                    filteredResults.addResult(filteredEntry);
                                }
                            }
                        }
                        break; // Stop searching the metadata for the current bookId
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Invalid ID format in metadata: " + bookIdString);
                }
            }
        }

        return filteredResults;
    }

    private void loadMetadataFromFile(File metadataFile) {
        Pattern pattern = Pattern.compile("(\\w+)\\s*:\\s*([^,]+)");
        metadata = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(metadataFile))) {
            String line;

            while ((line = reader.readLine()) != null) {
                Map<String, String> bookData = new HashMap<>();
                Matcher matcher = pattern.matcher(line);

                while (matcher.find()) {
                    String key = matcher.group(1);
                    String value = matcher.group(2);
                    bookData.put(key, value);
                }

                metadata.add(bookData);
            }
        } catch (IOException e) {
            System.err.println("Error reading metadata file: " + e.getMessage());
        }
    }

    public TextFragment getPartOfBookWithWord(Integer bookId, Integer wordId) {
        String fileRelativePath = PATH_TO_BOOKS_CONTENT_DIRECTORY + "/" + bookId + ".txt";
        File filePath = Paths.get(System.getProperty("user.dir"), fileRelativePath).toFile();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            int currPos = 0;
            String line;

            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\s+");
                currPos += words.length;

                if (currPos > wordId) {
                    int positionInLine = wordId - (currPos - words.length);
                    String lineContent = String.join(" ", words);
                    return new TextFragment(lineContent, positionInLine);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading book content: " + e.getMessage());
        }

        // If the wordId is not found, return an empty TextFragment or handle error accordingly
        return new TextFragment("Word not found", -1);
    }
}
