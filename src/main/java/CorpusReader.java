import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CorpusReader {

    private final Map<Integer, String> documents = new HashMap<>();

    /*
     When read the records of data set
     it creates new documents with unique id.
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

            while ((columns = reader.readNext()) != null) {

                // columns[2] -> title  columns[7] -> abstract   separate by whitespace
                this.documents.put(id_document, columns[2] + " " + columns[7]);
                id_document++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
