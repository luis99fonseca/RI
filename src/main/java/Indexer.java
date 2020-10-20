import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Indexer {

//    documents
    private Map<String, String> documents = new HashMap<>();
    public static int id_document = 0;
//    tokenizer
    private Tokenizer tokenizer;

//    index
    private final Map<Integer, String[]> inverted_index = new TreeMap<>();

    public Indexer(Map<String, String> documents, Tokenizer tokenizer) {
        this.documents = documents;
        this.tokenizer = tokenizer;
    }

    public Map<Integer, String[]> process_index(){
        for (String key : this.documents.keySet()) {
            String[] clean_words = this.tokenizer.process_tokens(documents.get(key));
            this.inverted_index.put(id_document, clean_words);
            id_document++;
        }
        return inverted_index;
    }

    public void setDocuments(Map<String, String> documents) {
        this.documents = documents;
    }
}
