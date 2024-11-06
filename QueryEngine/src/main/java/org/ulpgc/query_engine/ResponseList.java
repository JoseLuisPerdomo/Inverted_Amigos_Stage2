package org.ulpgc.query_engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResponseList<T> {
    private List<Map.Entry<Integer, T>> results = new ArrayList<>();

    public ResponseList() {}

    public ResponseList(List<Map.Entry<Integer, T>> results) {
        this.results = results;
    }

    public List<Map.Entry<Integer, T>> getResults() {
        return results;
    }

    public void setResults(List<Map.Entry<Integer, T>> results) {
        this.results = results;
    }

    public void addResult(Map.Entry<Integer, T> result) {
        this.results.add(result);
    }
}
