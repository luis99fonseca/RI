import com.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CorpusReader {

    private CSVReader csvReader;

    // TODO: mudar later out constrcut
    public CorpusReader(String csv_file) throws FileNotFoundException {
        csvReader = new CSVReader(new FileReader(csv_file));
    }

    private final Map<Integer, String> documents = new HashMap<>();

    /*
    For each document in the dataset,
     it creates a new entry with a unique id.
    Being static guarantees that there won't be repeated ids between different instantiations
    */
    public static int id_document = 0;

    public Map<Integer, String> getDocuments() {
        return documents;
    }


    public void loadDataCSV(String csv_file) {
        CSVReader reader = null;

        try {
            reader = new CSVReader(new FileReader(csv_file));
            String[] columns;

            reader.readNext(); // remove first line, as it contains the header

            System.out.println("lines read b4: " + reader.getLinesRead());

            while ((columns = reader.readNext()) != null) {
                if (!columns[7].isEmpty()) {
                    // columns[2] -> title;  columns[7] -> abstract;    separated by whitespace
                    this.documents.put(id_document, columns[2] + " " + columns[7]);
                }
                id_document++;
            }
            System.out.println("size: " + this.documents.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, String> readBlock() {
//        System.out.println("ola");
        try {
            String[] columns;
            Map<Integer, String> temp_docs = new HashMap<>();

            if (id_document == 0) {
                csvReader.readNext(); // remove first line, as it contains the header
            }

            int lines_read = 0;

            while ((columns = csvReader.readNext()) != null && lines_read < 10000) {
                if (!columns[7].isEmpty()) {
                    // columns[2] -> title;  columns[7] -> abstract;    separated by whitespace
                    temp_docs.put(id_document, columns[2] + " " + columns[7]);
                }
                id_document++;
                lines_read++;
            }
//            System.out.println("size: " + temp_docs.size() + "; read: " + id_document);
            return temp_docs;
        } catch (IOException e) {
            e.printStackTrace();
            return null;    //TODO: sera k faz sentido?
        }
    }
}
