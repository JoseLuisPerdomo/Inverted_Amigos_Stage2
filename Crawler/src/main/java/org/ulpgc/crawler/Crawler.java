package org.ulpgc.crawler;

import java.io.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler implements ICrawler {
    private final String baseUrl = "https://www.gutenberg.org";
    private final String outputDir = "gutenberg_books";

    public Crawler() {
        createOutputDirectory(outputDir);
    }

    private void createOutputDirectory(String dir) {
        File directory = new File(dir);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    private void downloadBookContent(String downloadLink, String bookFileName) {
        try {
            Document bookContent = Jsoup.connect(downloadLink).get();
            String textContent = bookContent.body().text();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(bookFileName, StandardCharsets.UTF_8))) {
                writer.write(textContent);
            }
        } catch (IOException e) {
            System.err.println("Error downloading book content from " + downloadLink + ": " + e.getMessage());
        }
    }

    private void saveMetadata(String id, String title, String author, String releaseDate, String language) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("gutenberg_data.txt", true))) {
            writer.write("ID: " + id + ", Title: " + title + ", Author: " + author +
                    ", Release Date: " + releaseDate + ", Language: " + language + "\n");
        } catch (IOException e) {
            System.err.println("Error saving metadata for book " + id + ": " + e.getMessage());
        }
    }

    private void downloadBook(String bookLink) {
        try {
            Document bookPage = Jsoup.connect(baseUrl + bookLink).get();
            String title = bookPage.selectFirst("h1").text();
            Element downloadElement = bookPage.selectFirst("a:contains(Plain Text UTF-8)");

            if (downloadElement != null) {
                String downloadLink = downloadElement.attr("href");
                if (!downloadLink.startsWith("http")) {
                    downloadLink = baseUrl + downloadLink;
                }

                String id = bookLink.split("/")[2];
                String bookFileName = outputDir + "/" + id + ".txt";

                downloadBookContent(downloadLink, bookFileName);

                String metadataText = bookPage.text();
                String author = extractMetadata(metadataText, "Author:");
                String releaseDate = extractMetadata(metadataText, "Release Date:");
                String language = extractMetadata(metadataText, "Language:");

                saveMetadata(id, title, author, releaseDate, language);

                System.out.println("Downloaded and saved book: " + title);
            } else {
                System.err.println("Plain Text UTF-8 format not available for " + bookLink);
            }
        } catch (IOException e) {
            System.err.println("Failed to download book at " + bookLink + ": " + e.getMessage());
        }
    }

    private String extractMetadata(String text, String label) {
        Pattern pattern = Pattern.compile(label + "\\s*(.+)");
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1).trim() : "Unknown";
    }

    @Override
    public void fetchBooks(int n) {
        try {
            Document searchPage = Jsoup.connect(baseUrl + "/ebooks/search/?sort_order=downloads").get();
            Elements bookLinks = searchPage.select("li.booklink a");

            int count = 0;
            for (Element link : bookLinks) {
                if (count >= n) break;
                downloadBook(link.attr("href"));
                count++;
            }
        } catch (IOException e) {
            System.err.println("Error fetching book list: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        crawler.fetchBooks(10);
    }
}
