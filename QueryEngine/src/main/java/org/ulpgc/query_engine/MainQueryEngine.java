package org.ulpgc.query_engine;

public class MainQueryEngine {
    public static void main(String[] args) {
        System.out.println("This is the Query Engine");
        SearchEngine searchEngine = new SearchEngine();
        ResponseList response = searchEngine.searchForBooksWithWord("cleopatra");
        System.out.println(response);
    }
}