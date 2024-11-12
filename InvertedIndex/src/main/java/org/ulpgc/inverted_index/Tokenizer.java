package org.ulpgc.inverted_index;

import java.util.Map;

public interface Tokenizer {
    public Map<String, ResponseList> tokenize(String book, int bookID);
}
