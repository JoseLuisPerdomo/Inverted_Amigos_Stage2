package org.ulpgc.inverted_index;

import java.util.Map;

public interface DatamartWriter {
    void write(Map<String, ResponseList> index);
}