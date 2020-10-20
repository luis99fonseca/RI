import java.util.Arrays;
import java.util.Map;

public class SecondMain {
    public static void main(String[] args) {

        final long startTime = System.nanoTime();

        String csv_file = "../data/all_sources_metadata_2020-03-13.csv";

        CorpusReader corpusReader = new CorpusReader();
//        Tokenizer tokenizer = new ImprovedTokenizer("src/snowball_stopwords_EN.txt");
        Tokenizer tokenizer = new SimpleTokenizer();
        Indexer indexer;

        corpusReader.loadDataCSV(csv_file);

        indexer = new Indexer(corpusReader.getDocuments(), tokenizer);

        Map<Integer, String[]> index = indexer.process_index();

        for (int key: index.keySet()){
            System.out.println(key + " -> " + Arrays.toString(index.get(key)));
        }

        final long endTime = System.nanoTime();
        System.out.println("Time: " + (endTime - startTime));

    }
}