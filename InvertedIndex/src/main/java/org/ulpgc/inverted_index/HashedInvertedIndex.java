package org.ulpgc.inverted_index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class HashedInvertedIndex implements InvertedIndex{

    private final File books;
    private final String datamart;
    private final Set<String> indexed;
    private final Tokenizer tokenizer;

    private final int numBuckets;

    public HashedInvertedIndex(String books, String datamart, String indexed, Tokenizer tokenizer, int numBuckets) {
        this.books = new File(books);
        this.datamart = datamart;
        this.indexed = this.getIndexed(new File(indexed));
        this.tokenizer = tokenizer;
        this.numBuckets = numBuckets;
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
            if (this.indexed.contains(id)){return 0;}
            return Integer.parseInt(id);
        }
        return 1;
    }

    @Override
    public void index(String file) {
        int id = isIndexed(file);
        switch (id) {
            case -1:
                System.out.println("This is not a book");
                break;
            case 0:
                System.out.println("Book Already indexed");
                break;
            default:
                Map<String, ResponseList> index = this.tokenizer.tokenize(file, id);
                System.out.println(index.get("chapter").getResults());
                Map<Integer, Map<String, ResponseList>> workload = distributeWorkload(index);
                updateDatamart(workload);
        }
    }

    private Map<Integer, Map<String, ResponseList>> distributeWorkload(Map<String, ResponseList> index) {
        Map<Integer, Map<String, ResponseList>> workLoad = new HashMap<>();

        // Inicializar los buckets
        for (int i = 0; i < this.numBuckets; i++) {
            workLoad.put(i, new HashMap<>());
        }

        // Recorre el índice original (newIndex) usando forEach
        index.forEach((palabra, responseList) -> {
            // Calcular el bucket usando la función hash
            int bucket = Math.abs(palabra.hashCode() % this.numBuckets);

            // Insertar la palabra y su ResponseList en el bucket correspondiente
            workLoad.get(bucket).put(palabra, responseList);
        });

        return workLoad;
    }

    private void updateDatamart(Map<Integer, Map<String, ResponseList>> index) {
        BinaryFileUpdaterWorkers[] threads = new BinaryFileUpdaterWorkers[numBuckets];

        for (int i = 0; i < numBuckets; i++) {
            BinaryDatamartReader reader = new BinaryDatamartReader(String.format(this.datamart, i));
            BinaryDatamartWriter writer = new BinaryDatamartWriter(String.format(this.datamart, i));
            threads[i] = new BinaryFileUpdaterWorkers(reader, writer, index.get(i));
            threads[i].start();
        }

        for (BinaryFileUpdaterWorkers thread: threads){
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public List<String> indexAll() {
        return this.listBooks();
    }

    public static void main(String[] args) {
        String books_path = "C:/Users/Eduardo/Desktop/gutenberg_books";
        String datamart = "C:/Users/Eduardo/Desktop/datamart/bucket_%s.dat";
        String books_indexed = "C:/Users/Eduardo/Desktop/indexed_docs.txt";
        String stopwords = "C:/Users/Eduardo/Desktop/stopwords.txt";
        Tokenizer tokenizer = new Tokenizer(stopwords);
        int numBuckets = 8;
        HashedInvertedIndex hashedInvertedIndex = new HashedInvertedIndex(books_path, datamart, books_indexed, tokenizer, numBuckets);
        //List<String> books_id = hashedInvertedIndex.indexAll();
        hashedInvertedIndex.index("C:/Users/Eduardo/Desktop/gutenberg_books/84_.txt");

    }
}
