import org.tartarus.snowball.ext.englishStemmer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class ImprovedTokenizer implements Tokenizer {

    private englishStemmer stemmer = new englishStemmer();
    private Set<String> stop_words;

    /*
    When this class is instantiated, 
    it loads stop words from the file passed (in command line or use default file) 
    to a Set looking to guarantee unique entries.
     */
    public ImprovedTokenizer(String stop_words_file) {
        stop_words = new HashSet<>();
        this.read_stop_words_file(stop_words_file);
    }

    private void read_stop_words_file(String stop_words_file) {
        try {
            File my_file = new File(stop_words_file);
            Scanner myReader = new Scanner(my_file);

            while (myReader.hasNextLine())
                this.stop_words.add(myReader.nextLine());

        } catch (FileNotFoundException e) {
            System.out.println("Stop Words File not Found!");
            e.printStackTrace();
        }
    }


    @Override
    public String[] process_tokens(String corpus) {
        String[] array_tokens = corpus.replaceAll("[^a-zA-Z0-9]", " ")             // only alphanumeric chars
                                        .replaceAll("\\b\\w{1,3}\\b\\s?", "")      // remove words less 3 chars
                                        .trim()                                                     // remove possible invisible chars created
                                        .toLowerCase()                                              // lowercase
                                        .split("\\s+");                                       // split one or more whitespaces


        List<String> list_tokens = new ArrayList<>(Arrays.asList(array_tokens));
        list_tokens.removeIf(this.stop_words::contains);    // remove stop_words from tokens

        list_tokens = this.stem_english_words(list_tokens); // clean stem

        return list_tokens.toArray(new String[0]);
    }

    /*
    Use suggested stemming to transform the word in a common "stem".
    Example: convert connection, connected, connecting  into connect.
     */
    private List<String> stem_english_words(List<String> list) {

        for(int i = 0 ; i < list.size(); i++) {
            this.stemmer.setCurrent(list.get(i));
            this.stemmer.stem();
            list.set(i, stemmer.getCurrent()) ;
        }

        return list;
    }
}
