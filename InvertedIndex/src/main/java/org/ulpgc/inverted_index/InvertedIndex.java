package org.ulpgc.inverted_index;

import java.util.List;

public interface InvertedIndex {
    public void index(String file);
    public List<String> indexAll();
}
