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

        return inverted_index;
    }


    /*
        Create Weight Matrix
        Df = Document Frequency = size of list associated the token
    */
    private void calculateTfIdfWeights(){

        for(String token : inverted_index.keySet())
            for(Post post: inverted_index.get(token))
                post.tfIdfWeighting(N, inverted_index.get(token).size() );  // TODO: save idf?
    }


}
