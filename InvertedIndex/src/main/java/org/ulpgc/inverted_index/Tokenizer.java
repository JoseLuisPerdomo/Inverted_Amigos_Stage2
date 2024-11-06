package org.ulpgc.inverted_index;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

// TODO
// Traducir al inglés

public class Tokenizer {

    private final String stopwords_file;
    public Tokenizer(String stopwords) {
        this.stopwords_file = stopwords;
    }

    public Map<String, List<Integer>> tokenize(String book){
        try {
            Set<String> stopwords = leerStopwords(this.stopwords_file);
            return procesarTexto(book, stopwords);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Leer las stopwords desde un archivo y almacenarlas en un Set
    private Set<String> leerStopwords(String rutaStopwords) throws IOException {
        return Files.lines(Paths.get(rutaStopwords))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    // Procesar el texto, eliminando puntuación y contando posiciones, ignorando stopwords para indexar
    private Map<String, List<Integer>> procesarTexto(String rutaTexto, Set<String> stopwords) throws IOException {
        Map<String, List<Integer>> palabrasConPosiciones = new HashMap<>();
        int posicionActual = 0;

        List<String> lineas = Files.lines(Paths.get(rutaTexto))
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        for (String linea : lineas) {
            // Eliminar puntuación y dividir en palabras
            String[] palabras = linea.replaceAll("[_,.;()]", "").split("\\s+");

            for (String palabra : palabras) {
                if (!palabra.isEmpty()) {  // Ignorar palabras vacías
                    if (!stopwords.contains(palabra)) {
                        // Agregar la palabra válida y su posición al mapa
                        palabrasConPosiciones
                                .computeIfAbsent(palabra, k -> new ArrayList<>())
                                .add(posicionActual);
                    }
                    // Incrementar la posición en cualquier caso (sea o no stopword)
                    posicionActual++;
                }
            }
        }

        return palabrasConPosiciones;
    }
}
