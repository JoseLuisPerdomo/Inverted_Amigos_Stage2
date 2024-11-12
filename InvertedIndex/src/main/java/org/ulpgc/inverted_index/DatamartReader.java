package org.ulpgc.inverted_index;

import java.util.Map;

public interface DatamartReader {
    Map<String, ResponseList> read();
    boolean exists();
}
