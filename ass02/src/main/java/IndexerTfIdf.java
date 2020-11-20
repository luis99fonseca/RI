import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class IndexerTfIdf extends Indexer  {


    public IndexerTfIdf(CorpusReader corpusReader, Tokenizer tokenizer) {
        super(corpusReader, tokenizer);
    }



    public Map<String, List<Post>> process_index(){

        //TODO: check this implementation better
        super.process_index();

        calculateTfIdfWeights();
        normalizeWt();

        return inverted_index;
    }


    private void calculateTfIdfWeights(){
        for(String token : inverted_index.keySet()){
            for(Post post: inverted_index.get(token)){
                post.tfIdfWeighting();
                countingTotalWeight(post);
            }
        }
    }




}
