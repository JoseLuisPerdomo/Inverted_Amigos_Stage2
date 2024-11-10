package org.ulpgc.inverted_index;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class HashedInvertedIndex implements InvertedIndex{

    private final File books;
    private final String datamart;
    private final Set<String> indexed;
    private final File indexedFile;
    private final Tokenizer tokenizer;

    private final int numBuckets;

    public HashedInvertedIndex(String books, String datamart, String indexed, Tokenizer tokenizer, int numBuckets) {
        this.books = new File(books);
        this.datamart = datamart;
        this.indexed = this.getIndexed(new File(indexed));
        this.tokenizer = tokenizer;
        this.numBuckets = numBuckets;
        this.indexedFile = new File(indexed);
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
        List<String> books_path = new ArrayList<>();
        File directory = this.books;
        if (directory.isDirectory()){
            File[] files = directory.listFiles();
            assert files != null;
            for (File file: files){
                if (file.isFile()){
                    books_path.add(file.getPath().replaceAll("\\\\", "/"));
                }
            }
        }
        return books_path;
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
    public void indexAll() {
        List<String> books = this.listBooks();
        for (String book: books){
            this.index(book);
        }
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
                System.out.println("Indexing book " + id);
                Map<String, ResponseList> index = this.tokenizer.tokenize(file, id);
                Map<Integer, Map<String, ResponseList>> workload = distributeWorkload(index);
                updateDatamart(workload);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.indexedFile, true))) { // 'true' para agregar al final
                    writer.write(id + ","); // Escribir el número seguido de una coma
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

    private void updateDatamart(Map<Integer, Map<String, ResponseList>> workload) {
        BinaryFileUpdaterWorkers[] threads = new BinaryFileUpdaterWorkers[numBuckets];

        for (int i = 0; i < numBuckets; i++) {
            BinaryDatamartReader reader = new BinaryDatamartReader(String.format(this.datamart, i));
            BinaryDatamartWriter writer = new BinaryDatamartWriter(String.format(this.datamart, i));
            threads[i] = new BinaryFileUpdaterWorkers(reader, writer, workload.get(i));
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

    public static void main(String[] args) {
        String books_path = "C:/Users/Eduardo/Desktop/gutenberg_books";
        String datamart = "C:/Users/Eduardo/Desktop/datamart/bucket_%s.dat";
        String books_indexed = "C:/Users/Eduardo/Desktop/indexed_docs.txt";
        String stopwords = "C:/Users/Eduardo/Desktop/stopwords.txt";
        Tokenizer tokenizer = new Tokenizer(stopwords);
        int numBuckets = 8;
        HashedInvertedIndex hashedInvertedIndex = new HashedInvertedIndex(books_path, datamart, books_indexed, tokenizer, numBuckets);
        //List<String> books_id = hashedInvertedIndex.indexAll();
        //hashedInvertedIndex.index("C:/Users/Eduardo/Desktop/gutenberg_books/84_.txt");
        hashedInvertedIndex.indexAll();

    }
}
