import com.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CorpusReader {

    private CSVReader csvReader;
    private Map<Integer, String> temp_docs = new HashMap<>();

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

    public Map<Integer, String> readBlock() {

        try {
            String[] columns;
            temp_docs.clear();

            if (id_document == 0) {
                csvReader.readNext(); // remove first line, as it contains the header
            }

            int lines_read = 0;

            while (lines_read < 5000 && (columns = csvReader.readNext()) != null) {
                if (!columns[7].isEmpty()) {

                    // columns[2] -> title;  columns[7] -> abstract;    separated by whitespace
                    temp_docs.put(id_document, columns[2] + " " + columns[7]);
                    lines_read++;
                }
                id_document++;
            }
            return temp_docs;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
