import com.opencsv.CSVReader;
import org.tartarus.snowball.ext.englishStemmer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CorpusReader {

    private final Map<String, String> documents = new HashMap<>();
    private final Map<Integer, String[]> tokens = new TreeMap<>();
    private final englishStemmer stemmer = new englishStemmer();

    public Map<String, String> getDocuments() {
        return documents;
    }

    public Map<Integer, String[]>  getTokens() {
        return tokens;
    }

    private List<String> englishStem(List<String> words) {

        for(int i = 0 ; i < words.size(); i++) {
            this.stemmer.setCurrent(words.get(i));
            this.stemmer.stem();
            words.set(i, stemmer.getCurrent()) ;
        }

        return words;
    }

    private Set<String> loadStopWordsEnglish() {

        Set<String> stop_words = new HashSet<>();
        String path_file = "src/snowball_stopwords_EN.txt";

        try {
            File my_file = new File(path_file);
            Scanner myReader = new Scanner(my_file);

            while(myReader.hasNextLine())
                stop_words.add(myReader.nextLine());

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        return stop_words;
    }


    private String[] cleanData(String key){
        return this.documents.get(key).replaceAll("[^A-Za-z0-9]", " ")   // remove non-alphabetic chars
                .replaceAll("\\b\\w{1,3}\\b\\s?", "")                    // remove words less 3 chars
                .toLowerCase()
                .split("\\s+");                                                     // split whitespaces
    };


    public void loadData(String csv_file) {
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

    public void simpleTokenizer() {
        int id_document = 0;

        for (String key : this.documents.keySet()) {
            String[] clean_words = this.cleanData(key);
            this.tokens.put(id_document, clean_words);
            id_document++;
        }
    }


    public void improvedTokenizer() {
        // stopwords
        // snowball
        int id_document = 0;
        Set<String> stop_words = this.loadStopWordsEnglish();
        List<String> words = new ArrayList<>();

        for (String key: this.documents.keySet()){
            String[] clean_words = this.cleanData(key);

            words.addAll(Arrays.asList(clean_words));
            words.removeIf(stop_words::contains); // remove stop_words from tokens

            this.tokens.put(id_document, englishStem(words).toArray(new String[0])); // clean stem

            id_document++;
            words.clear();
        }
    }


}
