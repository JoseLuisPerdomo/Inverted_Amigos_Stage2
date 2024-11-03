package org.ulpgc.query_engine;

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
        /*  @TODO
         *   Here will be implemented logic for reading from books
         *   and finding the line which contains the word with given
         *   position and then returning both the line and the position
         *   of the word in this line
         *   For now: it will just return something similar
         * */
        String line = String.format("this is some line from %d with word pos %d", bookId, wordId);
        return new TextFragment(line, wordId);
    }
}
