import java.util.*;

public class Indexer {

//    documents
    private Map<Integer, String> documents = new HashMap<Integer, String>();
    //public static int id_document = 0;
//    tokenizer
    private Tokenizer tokenizer;

    private final Map<String, Set<Integer>> inverted_index2 = new TreeMap<>();

    public Indexer(Map<Integer, String> documents, Tokenizer tokenizer) {
        this.documents = documents;
        this.tokenizer = tokenizer;
    }

    public Map<String, Set<Integer>> process_index(){
        for (Integer doc_id : this.documents.keySet()) {
            for(String token : this.tokenizer.process_tokens(documents.get(doc_id))){
                inverted_index2.computeIfAbsent(token, k -> new TreeSet<>());
                inverted_index2.get(token).add(doc_id);
            }

        }
        return inverted_index2;
    }

    public void setDocuments(Map<Integer, String> documents) {
        this.documents = documents;
    }
}
