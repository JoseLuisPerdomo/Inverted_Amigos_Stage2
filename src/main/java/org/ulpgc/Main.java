package org.ulpgc;

import org.ulpgc.crawler.MainCrawler;
import org.ulpgc.inverted_index.MainInvertedIndex;
import org.ulpgc.query_engine.MainQueryEngine;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Main Project...");


        MainCrawler.main(new String[]{});

        MainInvertedIndex.main(new String[]{});

        MainQueryEngine.main(new String[]{});
    }
}
