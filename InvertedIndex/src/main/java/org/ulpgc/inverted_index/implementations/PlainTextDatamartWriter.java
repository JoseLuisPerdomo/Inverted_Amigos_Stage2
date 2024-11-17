package org.ulpgc.inverted_index.implementations;

import org.ulpgc.inverted_index.apps.ResponseList;
import org.ulpgc.inverted_index.ports.DatamartWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlainTextDatamartWriter implements DatamartWriter {

    private final String datamart;

    public PlainTextDatamartWriter(String datamart) {
        this.datamart = datamart;
    }

    @Override
    public void write(Map<String, ResponseList> index) {
        index.forEach((word, responseList) -> {
            // Para cada palabra (word), obtenemos su ResponseList y procesamos las posiciones
            List<Map.Entry<Integer, List<Integer>>> results = responseList.getResults();

            // Recorremos cada resultado dentro del ResponseList
            for (Map.Entry<Integer, List<Integer>> entry : results) {
                Integer key = entry.getKey();  // El entero que representa la clave (por ejemplo, posición de un libro)
                String value = entry.getValue().stream()
                        .map(String::valueOf)  // Convertimos los valores (posiciones) a cadenas
                        .collect(Collectors.joining(", "));  // Unimos los valores separados por comas

                this.writeOnFile(word, String.valueOf(key), value);
            }
        });

    }
    private void writeOnFile(String word, String bookID, String appearances){
        File file = new File(String.format(this.datamart, word));
        if (!file.exists()){
            try {
                boolean newFile = file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println(word);
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) { // 'true' para agregar al final
            writer.write(bookID + ":" + appearances + "\n"); // Escribir el número seguido de una coma
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
