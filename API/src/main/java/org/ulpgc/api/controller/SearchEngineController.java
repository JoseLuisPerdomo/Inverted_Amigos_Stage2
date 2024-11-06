package org.ulpgc.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.ulpgc.query_engine.ResponseList;
import org.ulpgc.query_engine.SearchEngine;
import org.ulpgc.query_engine.TextFragment;

@RestController
public class SearchEngineController {

    private final SearchEngine searchEngine;

    public SearchEngineController() {
        this.searchEngine = new SearchEngine();
    }

    @GetMapping("/search/{indexer}")
    public ResponseList getSearchResults(
            @PathVariable String indexer,
            @RequestParam String word,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String language
    ) {
        return searchEngine.searchWithCriteria(indexer, word, title, author, date, language);
    }

    @GetMapping("/search")
    public ResponseList getSearchResults(@RequestParam String word) {
        return searchEngine.searchForBooksWithWord(word);
    }

    @GetMapping("/text")
    public TextFragment getTextFragment(
            @RequestParam Integer textId,
            @RequestParam Integer wordPos) {
        return searchEngine.getPartOfBookWithWord(textId, wordPos);
    }
}
