import java.lang.instrument.Instrumentation;
import java.util.*;

public class Indexer {

    private static Instrumentation instrumentation;

    private Map<Integer, String> documents;
    private CorpusReader corpusReader;

    private Tokenizer tokenizer;

    private final Map<String, Set<Integer>> inverted_index = new TreeMap<>();

    public Indexer(Map<Integer, String> documents, Tokenizer tokenizer) {
        this.documents = documents;
        this.tokenizer = tokenizer;
    }

    public Indexer(CorpusReader corpusReader, Tokenizer tokenizer) {
        this.corpusReader = corpusReader;
        this.tokenizer = tokenizer;
    }

    /*
    Given the documents and their tokens
    create inverted index as follows: 
        token -> [ document1_id, document2_id, ... ]
     */
    public Map<String, Set<Integer>> process_index(){

        Map<Integer, String> document_list;
        while ( !(document_list = this.corpusReader.readBlock()).isEmpty() ) {
            for (Integer doc_id : document_list.keySet()) {
                for (String token : this.tokenizer.process_tokens(document_list.get(doc_id))) {
                    if (!token.isEmpty()) {
                        inverted_index.computeIfAbsent(token, k -> new TreeSet<>());
                        inverted_index.get(token).add(doc_id);
                    }
                }
            }
        }

        return inverted_index;
    }

    public void setDocuments(Map<Integer, String> documents) {
        this.documents = documents;
    }
}
