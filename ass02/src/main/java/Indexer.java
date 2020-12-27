import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public abstract class  Indexer {

    public final CorpusReader corpusReader;
    public final Tokenizer tokenizer;
    public final Map<String, Double> total_weights = new HashMap<>();

    // Number of collections
    protected int N = 0;

    public final Map<String, List<Post>> inverted_index = new TreeMap<>();


    public Indexer(CorpusReader corpusReader, Tokenizer tokenizer) {
        this.corpusReader = corpusReader;
        this.tokenizer = tokenizer;
    }


    /*
   Given the documents and their tokens
   create inverted index as follows:
       token -> [ document1_id, document2_id, ... ]
    */
    public abstract Map<String, List<Post>> process_index();


    public void countingTotalWeight(Post post){
        String doc_id = post.getDocument_id();

        total_weights.putIfAbsent(doc_id, 0.0);
        total_weights.put(doc_id, total_weights.get(doc_id) + Math.pow(post.getWeight(), 2));
    }

    public void normalizeWt(){
        for(String token : inverted_index.keySet())
            for(Post post: inverted_index.get(token))
                post.setWeight( post.getWeight() / Math.sqrt(total_weights.get(post.getDocument_id())) );
    }

    public void writeInFile(String file_name){

        try{
            FileWriter myWriter = new FileWriter(file_name);

            for(String token : inverted_index.keySet()){
                /*
                Calculate IDF = log10(N/df)
                Df = Document Frequency = size of list associated the token
                */
                myWriter.write(token + ":" + Math.log10( (double) N/inverted_index.get(token).size()) + ";");

                for(Post post: inverted_index.get(token)){
                    myWriter.write(post.getDocument_id() + ":" + post.getWeight()+";");
                }

                myWriter.write("\n");
            }

            myWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
