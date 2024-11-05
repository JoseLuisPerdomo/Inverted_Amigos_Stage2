package org.ulpgc.crawler;


public class MainCrawler {
    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        crawler.fetchBooks(5);
    }
}
