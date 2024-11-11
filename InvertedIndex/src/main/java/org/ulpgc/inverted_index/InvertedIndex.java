package org.ulpgc.inverted_index;

import java.io.IOException;

public interface InvertedIndex {
    public void index(String file);
    public void indexBooks(String directory) throws IOException;
}
