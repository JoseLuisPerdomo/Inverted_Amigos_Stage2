package org.ulpgc.inverted_index.implementations;
import org.ulpgc.inverted_index.apps.ResponseList;
import org.ulpgc.inverted_index.ports.Tokenizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


public class GutenbergTokenizer implements Tokenizer {

    private final String stopwords_file;

    public GutenbergTokenizer(String stopwords_file) {
        this.stopwords_file = stopwords_file;
    }

    @Override
    public Map<String, ResponseList> tokenize(String book, int bookID) {
        try {
            Set<String> stopwords = reedStopwords(this.stopwords_file);
            return processText(book, stopwords, bookID);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Leer las stopwords desde un archivo y almacenarlas en un Set
    private Set<String> reedStopwords(String stopwordsFile) throws IOException {
        return Files.lines(Paths.get(stopwordsFile))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    // Procesar el texto, eliminando puntuación y contando posiciones, ignorando stopwords para indexar
    private Map<String, ResponseList> processText(String book, Set<String> stopwords, int bookID) throws IOException {
        Map<String, ResponseList> wordsWithPositions = new HashMap<>();
        int actualPosition = 0;

        // Leer el archivo línea por línea, convirtiendo a minúsculas
        List<String> lines = Files.lines(Paths.get(book))
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        for (String line : lines) {
            // Eliminar puntuación y dividir en palabras
            String[] words = line.replaceAll("[\\[\\]\\\\,.;:º\\dª&`´\\-_()¡\"!?¿{}=+<>|^“‘/$™%—•*”]", " ").split("\\s+");

            for (String word : words) {
                if (!word.isEmpty()) {  // Ignorar palabras vacías
                    if (!stopwords.contains(word)) {
                        // Obtener la ResponseList de la palabra o crear una nueva
                        ResponseList responseList = wordsWithPositions.computeIfAbsent(word, k -> new ResponseList());

                        // Buscar si ya existe una entrada para ese libro
                        Optional<Map.Entry<Integer, List<Integer>>> entryOpt = responseList.getResults().stream()
                                .filter(entry -> entry.getKey() == bookID)
                                .findFirst();

                        if (entryOpt.isPresent()) {
                            // Si ya existe una entrada para ese libro, agregar la posición
                            entryOpt.get().getValue().add(actualPosition);
                        } else {
                            // Si no existe, crear una nueva entrada para ese libro
                            List<Integer> positions = new ArrayList<>();
                            positions.add(actualPosition);
                            responseList.addResult(new AbstractMap.SimpleEntry<>(bookID, positions));
                        }
                    }
                    // Incrementar la posición en cualquier caso (sea o no stopword)
                    actualPosition++;
                }
            }
        }

        return wordsWithPositions;
    }
}
