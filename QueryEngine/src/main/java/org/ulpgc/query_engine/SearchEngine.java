package org.ulpgc.query_engine;

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchEngine {

    private List<Map<String, String>> metadata;
    private final String pathToMetadata = "data/metadata.txt";
    private final String pathToHashedIndex = "";
    private final String pathToDirectoryIndex = "indexes/directory";

    public enum Field {
        ID("ID"),
        TITLE("title"),
        AUTHOR("author");

        private final String value;

        Field(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public ResponseList searchForBooksWithWord(String word) {
        /*  @TODO
         *   Here will be implemented logic to get actual responses
         *   from the inverted index, firstly for one word
         *   For now it will just return something
         * */
        ResponseList list = searchInDirectoryIndex(word);
        return list;
    }

    private ResponseList searchInHashedIndex(String word) {
        // @TODO: implement this function
        return new ResponseList();
    }

    private ResponseList searchInDirectoryIndex(String word) {
        String pathToFileForWord = pathToDirectoryIndex + "/" + word.toLowerCase() + ".txt";
        File fileForWord = new File(System.getProperty("user.dir"), pathToFileForWord);
        ResponseList responseList = new ResponseList();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileForWord))) {
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
            System.err.println("Error reading metadata file: " + e.getMessage());
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
        if (metadata == null || metadata.isEmpty()) {
            File metadataFile = new File(System.getProperty("user.dir"), pathToMetadata);
            loadMetadataFromFile(metadataFile);
        }

        ResponseList filteredResults = new ResponseList();

        for (Map.Entry<Integer, List<Integer>> result : results.getResults()) {
            Integer bookId = result.getKey();
            for (Map<String, String> book : metadata) {
                if (Integer.parseInt(book.get("ID")) == bookId) {
                    if (book.get(field.getValue()).toLowerCase().contains(value.toLowerCase())) {
                        filteredResults.addResult(result);
                    }
                    break;
                }
            }
        }

        return filteredResults;
    }

    private void loadMetadataFromFile(File metadataFile) {
        Pattern pattern = Pattern.compile("'(\\w+)'\\s*:\\s*'([^']*)'");

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
        String fileRelativePath = "data/books_content/" + bookId + ".txt";
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
            e.printStackTrace();
        }

        // If the wordId is not found, return an empty TextFragment or handle error accordingly
        return new TextFragment("Word not found", -1);
    }
}
