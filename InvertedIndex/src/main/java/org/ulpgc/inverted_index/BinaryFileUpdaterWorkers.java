package org.ulpgc.inverted_index;

import java.util.Map;

public class BinaryFileUpdaterWorkers extends Thread implements UpdaterWorkers{

    private final DatamartReader reader;
    private final DatamartWriter writer;
    private final Map<String, ResponseList> index;

    public BinaryFileUpdaterWorkers(DatamartReader reader, DatamartWriter writer, Map<String, ResponseList> index) {
        this.reader = reader;
        this.writer = writer;
        this.index = index;
    }

    @Override
    public void run() {
        if (!reader.exists()){
            writer.write(this.index);
        }
        else{
            Map<String, ResponseList> savedIndex = reader.read();
            Map<String, ResponseList> updatedIndex = updateIndex(savedIndex, this.index);
            //System.out.println("Bucket " + this.bucket + " contiene " + updatedIndex.size() + " palabras.");
            writer.write(updatedIndex);
        }
    }

    private Map<String, ResponseList> updateIndex(Map<String, ResponseList> savedIndex, Map<String, ResponseList> index) {
        savedIndex.forEach((palabra, nuevaResponseList) -> {
            // Actualizar o insertar eficientemente
            savedIndex.merge(palabra, nuevaResponseList, (savedResponseList, nueva) -> {
                nueva.getResults().forEach(savedResponseList::addResult);
                return savedResponseList;
            });
        });

        return savedIndex;
    }
}
