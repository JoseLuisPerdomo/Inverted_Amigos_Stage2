package org.ulpgc.inverted_index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class HashedInvertedIndex implements InvertedIndex{

    private final File books;
    private final File datamart;
    private final Set<String> indexed;
    private final Tokenizer tokenizer;
    public HashedInvertedIndex(String books, String datamart, String indexed, Tokenizer tokenizer) {
        this.books = new File(books);
        this.datamart = new File(datamart);
        this.indexed = this.getIndexed(new File(indexed));
        this.tokenizer = tokenizer;
    }

    private Set<String> getIndexed(File indexed){
        try (BufferedReader br = new BufferedReader(new FileReader(indexed))) {
            String linea = br.readLine();  // Lee la única línea del archivo
            if (linea != null){
                return Arrays.stream(linea.split(","))
                        .collect(Collectors.toSet());
            }
            else {return new HashSet<>();}
        } catch (IOException e) {
            System.out.println("File not found");
        }
        return new HashSet<>();
    }

    public List<String> listBooks(){
        List<String> books_id = new ArrayList<>();
        File directory = books;
        if (directory.isDirectory()){
            File[] files = directory.listFiles();
            assert files != null;
            for (File file: files){
                if (file.isFile()){
                    books_id.add(file.getName().replaceAll("\\D", ""));
                }
            }
        }
        return books_id;
    }

    public int isIndexed(String file){
        String id = new File(file).getName().replaceAll("\\D", "");
        if (!id.isEmpty()){
            if (this.indexed.contains(id)){return 1;}
            return 0;
        }
        return -1;
    }

    @Override
    public void index(String file) {
        switch (isIndexed(file)) {
            case -1:
                System.out.println("This is not a book");
                break;
            case 1:
                System.out.println("Book Already indexed");
                break;
            case 0:
                Map<String, List<Integer>> index = this.tokenizer.tokenize(file);
                System.out.println(index.get("chapter"));
        }
    }

    @Override
    public List<String> indexAll() {
        return this.listBooks();
    }

    public static void main(String[] args) {
        String books_path = "C:/Users/Eduardo/Desktop/gutenberg_books";
        String datamart = "C:/Users/Eduardo/Desktop/datamart";
        String books_indexed = "C:/Users/Eduardo/Desktop/indexed_docs.txt";
        String stopwords = "C:/Users/Eduardo/Desktop/stopwords.txt";
        Tokenizer tokenizer = new Tokenizer(stopwords);
        HashedInvertedIndex hashedInvertedIndex = new HashedInvertedIndex(books_path, datamart, books_indexed, tokenizer);
        //List<String> books_id = hashedInvertedIndex.indexAll();
        hashedInvertedIndex.index("C:/Users/Eduardo/Desktop/gutenberg_books/84_.txt");
    }
}
