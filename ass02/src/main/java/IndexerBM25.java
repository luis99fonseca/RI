import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexerBM25 extends Indexer{

    Map<Integer, Integer> docs_len = new HashMap<>();
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

        Map<Integer, String> document_list;
        Post temp_post = new Post(-1);

        while ( !(document_list = corpusReader.readBlock()).isEmpty() ) {

            // counting the read documents to calculate idf
            N += document_list.size();

            for (Integer doc_id : document_list.keySet()) {

                int count_tokens = 0;

                for (String token : tokenizer.process_tokens(document_list.get(doc_id))) {

                    if (!token.isEmpty()) {

                        count_tokens++;

                        inverted_index.computeIfAbsent(token, k -> new ArrayList<>());

                        /*
                        to insert current doc_id to check if already exist some post with this id
                        if it exists, dont need create another post, just increase the freq
                         */
                        // TODO: maybe can need refactor
                        temp_post.setDocument_id(doc_id);
                        int i = inverted_index.get(token).indexOf(temp_post);

                        if( i != -1 )
                            inverted_index.get(token).get(i).increaseFreq();
                        else
                            inverted_index.get(token).add(new Post(doc_id));
                    }
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
            }
        }

    }

    private double calAvgDocLen(){
        return (double) total_len_docs / docs_len.size();
    }
}
