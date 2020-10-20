import org.tartarus.snowball.ext.englishStemmer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class ImprovedTokenizer implements Tokenizer {

    private Tokenizer simpleTokenizer = new SimpleTokenizer();
    private englishStemmer stemmer = new englishStemmer();
    private Set<String> stop_words;

    // TODO: maybe passar uma class StopWords?
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
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    @Override
    public String[] process_tokens(String corpus) {
        String[] array_tokens = simpleTokenizer.process_tokens(corpus);

        // TODO: not sure if the next block should be from classes of their own
        List<String> list_tokens = new ArrayList<>();

        list_tokens.addAll(Arrays.asList(array_tokens));
        list_tokens.removeIf(this.stop_words::contains);    // remove stop_words from tokens

        list_tokens = this.stem_english_words(list_tokens); // clean stem

        return list_tokens.toArray(new String[0]);
    }

    private List<String> stem_english_words(List<String> list) {

        for(int i = 0 ; i < list.size(); i++) {
            this.stemmer.setCurrent(list.get(i));
            this.stemmer.stem();
            list.set(i, stemmer.getCurrent()) ;
        }

        return list;
    }
}
