import java.lang.instrument.Instrumentation;
import java.util.*;

public class Indexer {

    private static Instrumentation instrumentation;

    private Map<Integer, String> documents;

    private Tokenizer tokenizer;

    private final Map<String, Set<Integer>> inverted_index = new TreeMap<>();

    public Indexer(Map<Integer, String> documents, Tokenizer tokenizer) {
        this.documents = documents;
        this.tokenizer = tokenizer;
    }

    /*
    Given the documents and theirs tokens creates
    inverted index as follows:
        token -> [ document1_id, document2_id, ... ]
     */
    public Map<String, Set<Integer>> process_index(){

        for (Integer doc_id : this.documents.keySet()) {
            for(String token : this.tokenizer.process_tokens(documents.get(doc_id))){
                if(!token.isEmpty()){
                    inverted_index.computeIfAbsent(token, k -> new TreeSet<>());
                    inverted_index.get(token).add(doc_id);
                }
            }
        }

        return inverted_index;
    }

    public void setDocuments(Map<Integer, String> documents) {
        this.documents = documents;
    }
}
