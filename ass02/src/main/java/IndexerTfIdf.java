import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class IndexerTfIdf extends Indexer  {


    public IndexerTfIdf(CorpusReader corpusReader, Tokenizer tokenizer) {
        super(corpusReader, tokenizer);
    }

    public Map<String, List<Post>> process_index(){
        Map<String, String> document_list;

        while ( !(document_list = this.corpusReader.readBlock()).isEmpty() ) {

            // counting the number of read documents to calculate idf
            N += document_list.size();

            for (String doc_id : document_list.keySet()) {
                Map<String, Integer> temp_freq_tokens = new HashMap<>();
                Map<String, List<Integer>> temp_list_pos = new HashMap<>();
                int pos = 0;

                for (String token : this.tokenizer.process_tokens(document_list.get(doc_id))) {
                    if (!token.isEmpty()) {
//                        temp_freq_tokens.computeIfAbsent(token, k->0);
                        temp_freq_tokens.putIfAbsent(token, 0);
                        temp_freq_tokens.put( token, temp_freq_tokens.get(token) + 1 );

                        // extend the index to the position of the term in the doc
                        pos += 1;
                        temp_list_pos.putIfAbsent(token, new ArrayList<>());
                        temp_list_pos.get(token).add(pos);
                    }
                }

                for(String token: temp_freq_tokens.keySet()){
                    inverted_index.computeIfAbsent(token, k-> new ArrayList<>());

                    Post new_post = new Post(doc_id, temp_freq_tokens.get(token), temp_list_pos.get(token));
                    new_post.tfIdfWeighting();
                    countingTotalWeight(new_post);

                    inverted_index.get(token).add(new_post);
                }
            }
        }
        normalizeWt();

        return inverted_index;
    }

}
