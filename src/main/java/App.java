import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/*
Run as follows:
    - compile:
        mvn compile
    - run:
        mvn exec:java -Dexec.args=""
 */
public class App {
    public static void main(String[] args) {

        final long startTime = System.nanoTime();
        System.out.println(">>> " + Arrays.toString(args));

        String csv_file = "src/all_sources_metadata_2020-03-13.csv";
        String stop_words_file = "src/snowball_stopwords_EN.txt";

        if (args.length == 2) {
            csv_file = args[0];
            stop_words_file = args[1];
        }

        CorpusReader corpusReader = new CorpusReader();
        //Tokenizer tokenizer = new ImprovedTokenizer(stop_words_file);
        Tokenizer tokenizer = new SimpleTokenizer();
        Indexer indexer;

        corpusReader.loadDataCSV(csv_file);

        indexer = new Indexer(corpusReader.getDocuments(), tokenizer);

        Map<String, Set<Integer>> index = indexer.process_index();

        for (String key : index.keySet()) {
            System.out.println(key + "-> " + index.get(key));
        }

        final long endTime = System.nanoTime();
        System.out.println("Time: " + (endTime - startTime));


    }
}


