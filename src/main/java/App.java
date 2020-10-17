import org.tartarus.snowball.ext.englishStemmer;

import java.util.Arrays;
import java.util.Map;

public class App {
    public static void main(String[] args) {

        String csv_file = "src/all_sources_metadata_2020-03-13.csv";
        CorpusReader my_reader = new CorpusReader();

        my_reader.loadData(csv_file);

        // choice tokenizer
        //my_reader.simpleTokenizer();
        my_reader.improvedTokenizer();

        Map<Integer, String[]> data = my_reader.getTokens();

        for (int key: data.keySet()){
            System.out.println(key + " -> " + Arrays.toString(data.get(key)));
        }

    }
}
