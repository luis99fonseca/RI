import java.util.Arrays;
import java.util.Map;

public class App {
    public static void main(String[] args) {

        final long startTime = System.nanoTime();
        String csv_file = "../data/all_sources_metadata_2020-03-13.csv";
        CorpusReader my_reader = new CorpusReader();

        my_reader.loadDataCSV(csv_file);

        ATokenizer my_tokenizer = new ATokenizer(my_reader.getDocuments());

        //choice tokenizer
//        my_tokenizer.improvedTokenizer();
        my_tokenizer.simpleTokenizer();

        Map<Integer, String[]> data = my_tokenizer.getTokens();

        for (int key: data.keySet()){
            System.out.println(key + " -> " + Arrays.toString(data.get(key)));
        }

        final long endTime = System.nanoTime();
        System.out.println("Time: " + (endTime - startTime));

    }
}
