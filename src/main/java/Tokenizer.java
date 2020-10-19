import org.tartarus.snowball.ext.englishStemmer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Tokenizer {

    private Map<String, String> documents = new HashMap<>();
    private final englishStemmer stemmer = new englishStemmer();
    private final Map<Integer, String[]> tokens = new TreeMap<>();
    static int id_document = 0;

    public Tokenizer(Map<String, String> documents){
        this.documents = documents;
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

        try {
            File my_file = new File("src/snowball_stopwords_EN.txt");
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

    public void simpleTokenizer() {

        for (String key : this.documents.keySet()) {
            String[] clean_words = this.cleanData(key);
            this.tokens.put(id_document, clean_words);
            id_document++;
        }
    }

    public void improvedTokenizer() {
        // stopwords
        // snowball
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
