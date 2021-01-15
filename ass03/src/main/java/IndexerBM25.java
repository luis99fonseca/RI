import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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


    public Map<String, List<Post>> processIndexWithoutPositions() {
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

    @Override
    public Map<String, List<Post>> processIndexWithPositions() {
        Map<String, String> document_list;

        while ( !(document_list = corpusReader.readBlock()).isEmpty() ) {

            // counting the read documents to calculate idf
            N += document_list.size();

            for (String doc_id : document_list.keySet()) {
                Map<String, Integer> temp_freq_tokens = new HashMap<>();
                Map<String, List<Integer>> temp_list_pos = new HashMap<>();
                int count_tokens = 0;
                int pos = 0;

                for (String token : tokenizer.process_tokens(document_list.get(doc_id))) {

                    if (!token.isEmpty()) {
                        count_tokens++;
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
                    inverted_index.get(token).add(new Post(doc_id, temp_freq_tokens.get(token), temp_list_pos.get(token)));
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

    public Map<String, List<Post>> processIndexWithMerge(){
        Map<String, String> document_list;
        int blocks_read = 0;

        while ( !(document_list = corpusReader.readBlock()).isEmpty() ) {

            // counting the read documents to calculate idf
            N += document_list.size();
            blocks_read += 1;

            for (String doc_id : document_list.keySet()) {
                Map<String, Integer> temp_freq_tokens = new HashMap<>();
                Map<String, List<Integer>> temp_list_pos = new HashMap<>();
                int count_tokens = 0;
                int pos = 0;

                for (String token : tokenizer.process_tokens(document_list.get(doc_id))) {

                    if (!token.isEmpty()) {
                        count_tokens++;
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
                    inverted_index.get(token).add(new Post(doc_id, temp_freq_tokens.get(token), temp_list_pos.get(token)));
                }

                docs_len.put(doc_id, count_tokens);
                total_len_docs += count_tokens;
            }
            createTempFile(blocks_read);
            docs_len.clear();
//            total_len_docs = 0 ??? //TODO: ter em conta a memoria usada tbm crl
            inverted_index.clear();
        }
//        calBM25Ranking();

        return inverted_index;
    }

    @Override
    public void mergeFiles() throws IOException {

    }

    protected void createTempFile(int file_number) {
        //System.out.println("aa");
        try{
            Files.createDirectories(Paths.get("bm25_temp_files")); //TODO. mudar pa temp_files, so ta assim pa development

            FileWriter myWriter = new FileWriter("bm25_temp_files/temp_iindex_" + String.format("%02d", file_number) + ".txt");

            for(String token : inverted_index.keySet()){
                /*
                Calculate IDF = log10(N/df)
                Df = Document Frequency = size of list associated the token
                */
//                myWriter.write(token + ":" + Math.log10( (double) N/inverted_index.get(token).size()) + ";");
                myWriter.write(token + ";");
                for(Post post: inverted_index.get(token)){
                    myWriter.write(post.getDocument_id() + ":" + docs_len.get(post.getDocument_id()) + "-" + post.getFreqToken() + ":" + post.getTextPositions() + ";");
                }

                myWriter.write("\n");
            }

            myWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
