import java.io.FileWriter;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.*;

public class Indexer {


    private CorpusReader corpusReader;
    private Tokenizer tokenizer;

    // Number of collections
    private int N = 0;

    private final Map<String, List<Post>> inverted_index = new TreeMap<>();


    public Indexer(CorpusReader corpusReader, Tokenizer tokenizer) {
        this.corpusReader = corpusReader;
        this.tokenizer = tokenizer;
    }

    /*
    Given the documents and their tokens
    create inverted index as follows: 
        token -> [ document1_id, document2_id, ... ]
     */
    public Map<String, List<Post>> process_index(){

        Map<Integer, String> document_list;
        Post temp_post = new Post(-1);

        while ( !(document_list = this.corpusReader.readBlock()).isEmpty() ) {

            // counting the read documents to calculate idf
            N += document_list.size();

            for (Integer doc_id : document_list.keySet()) {
                for (String token : this.tokenizer.process_tokens(document_list.get(doc_id))) {
                    if (!token.isEmpty()) {
                        inverted_index.computeIfAbsent(token, k -> new ArrayList<>());

                        // TODO: maybe can need refactor
                        temp_post.setDocument_id(doc_id);
                        int i = inverted_index.get(token).indexOf(temp_post);

                        if( i != -1 )
                            inverted_index.get(token).get(i).increaseFreq();
                        else
                            inverted_index.get(token).add(new Post(doc_id));
                    }
                }
            }
        }
        return inverted_index;
    }


    /*
        Create Weight Matrix
        Df = Document Frequency = size of list associated the token
    */
    public Map<String, List<Post>> calculateTfIdfWeights(String file_name){

        for(String token : inverted_index.keySet())
            for(Post post: inverted_index.get(token))
                post.tfIdfWeighting(N, inverted_index.get(token).size() );

        writeInFile(file_name);

        return inverted_index;
    }


    private void writeInFile(String file_name){

        try{
            FileWriter myWriter = new FileWriter(file_name);
            for(String token : inverted_index.keySet()){
                myWriter.write(token + ": " + Math.log( (double) N/inverted_index.get(token).size()) + "; ");
                for(Post post: inverted_index.get(token)){
                    myWriter.write(post.getDocument_id() + ":" + post.getWeight()+"; ");
                }
                myWriter.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
