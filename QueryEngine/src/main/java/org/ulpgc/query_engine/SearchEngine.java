package org.ulpgc.query_engine;

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class SearchEngine {

    public ResponseList searchForBooksWithWord(String word) {
        /*  @TODO
         *   Here will be implemented logic to get actual responses
         *   from the inverted index, firstly for one word
         *   For now it will just return something
         * */
        ResponseList list = new ResponseList();
        Map.Entry<Integer, List<Integer>> entry = new AbstractMap.SimpleEntry<>(15, List.of(12, 13, 14));
        list.addResult(entry);
        return list;
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
