import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexerBM25 extends Indexer{

    Map<String, Integer> docs_len = new HashMap<>();
    long total_len_docs = 0;

    // parameters of BM25
    double k, b;


    public IndexerBM25(CorpusReader corpusReader, Tokenizer tokenizer, double k, double b) {
        super(corpusReader, tokenizer);
        this.k = k;
        this.b = b;
    }

    @Override
    public Map<String, List<Post>> process_index() {
        Map<String, String> document_list;

        while ( !(document_list = corpusReader.readBlock()).isEmpty() ) {

            // counting the read documents to calculate idf
            N += document_list.size();

            for (String doc_id : document_list.keySet()) {
                Map<String, Integer> temp_freq_tokens = new HashMap<>();
                int count_tokens = 0;

                for (String token : tokenizer.process_tokens(document_list.get(doc_id))) {

                    if (!token.isEmpty()) {
                        count_tokens++;
                        temp_freq_tokens.putIfAbsent(token, 0);
                        temp_freq_tokens.put( token, temp_freq_tokens.get(token) + 1 );
                    }
                }

                for(String token: temp_freq_tokens.keySet()){
                    inverted_index.computeIfAbsent(token, k-> new ArrayList<>());
                    inverted_index.get(token).add(new Post(doc_id, temp_freq_tokens.get(token)));
                }

                docs_len.put(doc_id, count_tokens);
                total_len_docs += count_tokens;
            }
        }

        calBM25Ranking();

        return inverted_index;
    }

    private void calBM25Ranking(){

        double avdl = calAvgDocLen();

        for(String token : inverted_index.keySet()){
            for(Post post: inverted_index.get(token)){
                post.BM25(k, b, avdl, docs_len.get( post.getDocument_id() ) , N, inverted_index.get(token).size());
                countingTotalWeight(post);
            }
        }

    }


    private double calAvgDocLen(){
        return (double) total_len_docs / docs_len.size();
    }
}
