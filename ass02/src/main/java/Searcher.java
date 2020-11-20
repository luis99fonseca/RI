import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Searcher {

    private Map<String, List<Post>> inverted_index;
    private Map<String, Double> idfs = new TreeMap<>();
    private Tokenizer tokenizer;


    public Searcher(String name_file, Tokenizer tokenizer){
        // load indexer
        loadIndexer(name_file);
        this.tokenizer = tokenizer;

    }

    // TODO: maybe remove or not
    public Searcher(Map<String, List<Post>> inverted_index){
        this.inverted_index = inverted_index;
    }


    private void loadIndexer(String file_name){
        inverted_index = new TreeMap<>();

        try{
            File myFile = new File(file_name);
            Scanner myReader = new Scanner(myFile);

            while (myReader.hasNextLine()) {

                String data = myReader.nextLine();
                String[] cols = data.split(";");
                List<Post> docs = new ArrayList<>();

                for(int i = 1; i < cols.length; i++){
                    String[] attr = cols[i].split(":");
                    docs.add(new Post(attr[0], Double.parseDouble(attr[1])));
                }

                String[] token_info = cols[0].split(":");
                inverted_index.put(token_info[0], docs);
                idfs.put(token_info[0], Double.parseDouble(token_info[1]));
            }

            myReader.close();

        } catch (FileNotFoundException e) {
            System.err.println("Error: Fail read indexer file!");
            e.printStackTrace();
        }
    }

    public Map<String, Double> searchingLncLtc(String input){

        Map<String, Post> query = new HashMap<>();

        //System.out.println(inverted_index.size());


        for(String token: this.tokenizer.process_tokens(input)){
            if (!token.isEmpty() && !query.containsKey(token)) {
                query.put(token, new Post());
            }
            query.get(token).increaseFreq();
        }

         // Calculate tokens of query weights
        double total_wt_query = 0.0;

        for(String token: query.keySet()){
            if(idfs.containsKey(token)){
                query.get(token).tfIdfWeighting( idfs.get(token) );
                total_wt_query += Math.pow(query.get(token).getWeight(), 2);
            }
        }

        //Normalize and Score
        Map<String, Double> scores = new HashMap<>();

        for(String token: query.keySet()){
            if(idfs.containsKey(token)){
                Post query_post = query.get(token);
                query_post.setWeight( query_post.getWeight() / Math.sqrt(total_wt_query) );

                for(Post post : inverted_index.get(token)){
                    String doc_id = post.getDocument_id();
                    scores.computeIfAbsent(doc_id, k -> 0.0);
                    scores.put(doc_id, scores.get(doc_id) + (post.getWeight() * query_post.getWeight()));
                }
            }
        }

        Map<String, Double> top_scores = new LinkedHashMap<>();

        //sort map
        scores.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(x -> top_scores.put(x.getKey(), x.getValue()));

        return top_scores;
    }


    public Map<String, Double> searchingBM25(String input){

        Map<String, Double> scores = new HashMap<>();

        for(String token: this.tokenizer.process_tokens(input)){
            if (!token.isEmpty() && inverted_index.containsKey(token) ) {

                for(Post post: inverted_index.get(token)){
                    String doc_id = post.getDocument_id();
                    scores.computeIfAbsent(doc_id, k -> 0.0);
                    scores.put(doc_id, scores.get(doc_id) + post.getWeight());
                }
            }
        }

        Map<String, Double> top_scores = new LinkedHashMap<>();

        //sort map
        scores.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(x -> top_scores.put(x.getKey(), x.getValue()));


        return top_scores;
    }

    private void loadQueries(String name_file){

    }

    public Map<String, List<Post>> getInverted_index() {
        return inverted_index;
    }
}
