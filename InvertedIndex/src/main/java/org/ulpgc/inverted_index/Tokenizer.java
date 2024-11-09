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

    public Tokenizer(String stopwords_file) {
        this.stopwords_file = stopwords_file;
    }

    public Map<String, ResponseList> tokenize(String book, int libroID) {
        try {
            Set<String> stopwords = leerStopwords(this.stopwords_file);
            return procesarTexto(book, stopwords, libroID);
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
    private Map<String, ResponseList> procesarTexto(String rutaTexto, Set<String> stopwords, int libroID) throws IOException {
        Map<String, ResponseList> palabrasConPosiciones = new HashMap<>();
        int posicionActual = 0;

        // Leer el archivo línea por línea, convirtiendo a minúsculas
        List<String> lineas = Files.lines(Paths.get(rutaTexto))
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        for (String linea : lineas) {
            // Eliminar puntuación y dividir en palabras
            String[] palabras = linea.replaceAll("[_,.;()]", "").split("\\s+");

            for (String palabra : palabras) {
                if (!palabra.isEmpty()) {  // Ignorar palabras vacías
                    if (!stopwords.contains(palabra)) {
                        // Obtener la ResponseList de la palabra o crear una nueva
                        ResponseList responseList = palabrasConPosiciones.computeIfAbsent(palabra, k -> new ResponseList());

                        // Buscar si ya existe una entrada para ese libro
                        Optional<Map.Entry<Integer, List<Integer>>> entryOpt = responseList.getResults().stream()
                                .filter(entry -> entry.getKey() == libroID)
                                .findFirst();

                        if (entryOpt.isPresent()) {
                            // Si ya existe una entrada para ese libro, agregar la posición
                            entryOpt.get().getValue().add(posicionActual);
                        } else {
                            // Si no existe, crear una nueva entrada para ese libro
                            List<Integer> posiciones = new ArrayList<>();
                            posiciones.add(posicionActual);
                            responseList.addResult(new AbstractMap.SimpleEntry<>(libroID, posiciones));
                        }
                    }
                    // Incrementar la posición en cualquier caso (sea o no stopword)
                    posicionActual++;
                }
            }
        }

        return palabrasConPosiciones;
    }
}
