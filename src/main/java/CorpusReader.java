import com.opencsv.CSVReader;
import org.tartarus.snowball.ext.englishStemmer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CorpusReader {

    private final Map<String, String> documents = new HashMap<>();


    public Map<String, String> getDocuments() {
        return documents;
    }

    public void loadDataCSV(String csv_file) {
        CSVReader reader = null;

        try {
            reader = new CSVReader(new FileReader(csv_file));
            String[] columns;
            int counter = 0; // only for test

            reader.readNext(); // remove first line

            while ((columns = reader.readNext()) != null && counter<10) {
                this.documents.put(columns[0], columns[2] + " " + columns[7]);
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
