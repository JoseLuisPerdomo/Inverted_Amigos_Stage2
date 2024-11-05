package org.ulpgc.inverted_index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HashedInvertedIndex implements InvertedIndex{

    private final File books;
    private final File datamart;
    private final File indexed;
    public HashedInvertedIndex(String books, String datamart, String indexed) {
        this.books = new File(books);
        this.datamart = new File(datamart);
        this.indexed = new File(indexed);
    }

    public List<String> listBooks(){
        Pattern pattern = Pattern.compile("d", Pattern.CASE_INSENSITIVE);
        List<String> books_id = new ArrayList<String>();
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

    public boolean isIndexed(String id){
        try (BufferedReader br = new BufferedReader(new FileReader(this.indexed))) {
            String linea = br.readLine();  // Lee la única línea del archivo
            if (!linea.isEmpty()){
                Set<String> numeros = Arrays.stream(linea.split(","))
                        .collect(Collectors.toSet());
                return numeros.contains(id);
            }
            else {return false;}
        } catch (IOException e) {
            System.out.println("File not found");
        }
        return false;
    }

    public 
    @Override
    public void index(String file) {

    }

    @Override
    public List<String> indexAll() {
        List<String> books_id = this.listBooks();

        return books_id;
    }

    public static void main(String[] args) {
        String books_path = "C:/Users/Eduardo/Desktop/gutenberg_books";
        String datamart = "C:/Users/Eduardo/Desktop/datamart";
        String books_indexed = "C:/Users/Eduardo/Desktop/indexed_docs.txt";
        HashedInvertedIndex hashedInvertedIndex = new HashedInvertedIndex(books_path, datamart, books_indexed);
        //List<String> books_id = hashedInvertedIndex.indexAll();
        System.out.println(hashedInvertedIndex.isIndexed("25"));
    }
}
