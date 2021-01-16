import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.net.FileNameMap;
import java.util.*;

public class Searcher {

    private TreeMap<String, List<Post>> inverted_index = new TreeMap<>();
    private Map<String, Double> idfs = new TreeMap<>();
    private Tokenizer tokenizer;
    private String directory_name;
    private double max_mbs;
    public Map<String, Integer> freq_terms_searched = new HashMap<>();


    public Searcher(String name_file, Tokenizer tokenizer){
        // load indexer
        loadIndexer(name_file);
        this.tokenizer = tokenizer;
    }

    public Searcher(Tokenizer tokenizer, String directory_name, double max_mbs){
        this.directory_name = directory_name;
        this.tokenizer = tokenizer;
        this.max_mbs = max_mbs;
    }

    private void check_memory_to_load_indexer(double percentage_to_remove){

        Runtime runtime = Runtime.getRuntime();
        double memoryUsed = (runtime.totalMemory() - runtime.freeMemory()) * Math.pow(10,-6);

        if (memoryUsed >= max_mbs){
            Map<String, Integer> sorted_freq_terms = sortMapByValueInteger(freq_terms_searched);
            Set<String> terms = sorted_freq_terms.keySet();

            int elements_to_delete = (int) percentage_to_remove * terms.size();
            int count = 0;

            for(String term : terms){

                if(elements_to_delete == count)
                    break;

                freq_terms_searched.remove(term);

                inverted_index.remove(term);

                count++;
            }
            System.gc(); // free memory
        }
    }

    private void loadIndexer(String file_name){

        String[] debug = new String[0];
        try{
            File myFile = new File(file_name);
            Scanner myReader = new Scanner(myFile);

            while (myReader.hasNextLine()) {

                String data = myReader.nextLine();
                String[] cols = data.split(";");
                List<Post> docs = new ArrayList<>();

                for(int i = 1; i < cols.length; i++){
                    String[] attr = cols[i].split(":");

                    if (attr.length == 2)

                        // without positions
                        docs.add(new Post(attr[0], Double.parseDouble(attr[1])));

                    else {

                        // with positions
                        String[] pos = attr[2].split(",");
                        List <Integer> positions = new ArrayList<>();

                        for (String p : pos) {
                            positions.add(Integer.parseInt(p));
                        }

                        docs.add(new Post(attr[0], Double.parseDouble(attr[1]), positions));
                    }
                }

                String[] token_info = cols[0].split(":");
                debug = token_info;

                check_memory_to_load_indexer(0.10);
                inverted_index.put(token_info[0], docs);
                idfs.put(token_info[0], Double.parseDouble(token_info[1]));
            }

            myReader.close();

        } catch (FileNotFoundException e) {
            System.err.println("Error: Fail read indexer file!");
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e){
            System.err.println("Error: Fail read split! = ");
            System.out.println(Arrays.toString(debug));
            e.printStackTrace();
        }
    }

    public void check_index(String[] query){

        File directory = new File(directory_name);

        FilenameFilter filter = (f1, name) -> name.endsWith(".txt");

        String[] files = directory.list(filter);

        assert files != null;

        for(String term : query){

            if(!inverted_index.containsKey(term)){

                for(String name_file : files){

                   String[] range_terms = name_file.split("\\.")[0].split("-");

                   if ((range_terms[0].compareTo(term) <= 0 && term.compareTo(range_terms[1]) <=0 ))
                       loadIndexer(directory_name + "/" + name_file);


                }
            }
        }

        //System.out.println(inverted_index);

    }

    public Map<String, Double> searchingLncLtcWithoutPositions(String input, int n_top_docs){

        Map<String, Post> query = new LinkedHashMap<>();

        //System.out.println(inverted_index.size());
        String[] terms = this.tokenizer.process_tokens(input);

        for(String token: terms){
            if (!token.isEmpty()){
                query.putIfAbsent(token, new Post());
                query.get(token).increaseFreq();
                freq_terms_searched.putIfAbsent(token, 0);
                freq_terms_searched.put(token, freq_terms_searched.get(token) + 1);
            }
        }

        check_index(terms);

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
                    scores.putIfAbsent(doc_id, 0.0);
                    scores.put(doc_id, scores.get(doc_id) + (post.getWeight() * query_post.getWeight()));
                }
            }
        }

        return sortMapByValue(scores, n_top_docs);
    }

    public Map<String, Double> searchingLncLtcWithPositions(String input, int n_top_docs, double match_score,
                                                            int margin_window_error, double penalization_score){

        Map<String, Post> query = new LinkedHashMap<>();

        //System.out.println(inverted_index.size());
        String[] terms = this.tokenizer.process_tokens(input);

        for(String token: terms){
            if (!token.isEmpty()){
                query.putIfAbsent(token, new Post());
                query.get(token).increaseFreq();
                freq_terms_searched.putIfAbsent(token, 0);
                freq_terms_searched.put(token, freq_terms_searched.get(token) + 1);
            }
        }

        check_index(terms);

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
                    scores.putIfAbsent(doc_id, 0.0);
                    scores.put(doc_id, scores.get(doc_id) + (post.getWeight() * query_post.getWeight()));
                }
            }
        }
        Map<String, Double> docs_boost = boostRankWithPositions(query, match_score, margin_window_error, penalization_score);

        // TODO: maybe need function to smooth the results | check if doc_boost exist in scores
        for(String doc_id : docs_boost.keySet())
            scores.put(doc_id, scores.get(doc_id) + Math.log10(docs_boost.get(doc_id)));

        return sortMapByValue(scores, n_top_docs);
    }


    public Map<String, Double> searchingBM25WithoutPositions(String input, int n_top_docs){

        Map<String, Double> scores = new HashMap<>();

        String[] terms = this.tokenizer.process_tokens(input);

        check_index(terms);

        for(String token: terms){
            if (!token.isEmpty() && inverted_index.containsKey(token) ) {

                freq_terms_searched.putIfAbsent(token, 0);
                freq_terms_searched.put(token, freq_terms_searched.get(token) + 1);

                for(Post post: inverted_index.get(token)){
                    String doc_id = post.getDocument_id();
                    scores.putIfAbsent(doc_id, 0.0);
                    scores.put(doc_id, scores.get(doc_id) + post.getWeight());
                }
            }
        }

        return sortMapByValue(scores, n_top_docs);
    }

    public Map<String, Double> searchingBM25WithPositions(String input, int n_top_docs, double match_score,
                                                          int margin_window_error, double penalization_score){

        Map<String, Double> scores = new HashMap<>();
        Map<String, Post> query = new LinkedHashMap<>();
        String[] terms = this.tokenizer.process_tokens(input);

        check_index(terms);

        for(String token: terms){
            if (!token.isEmpty())
                query.putIfAbsent(token, new Post());
        }

        for(String token: query.keySet()){
            if (!token.isEmpty() && inverted_index.containsKey(token) ) {

                freq_terms_searched.putIfAbsent(token, 0);
                freq_terms_searched.put(token, freq_terms_searched.get(token) + 1);

                for(Post post: inverted_index.get(token)){
                    String doc_id = post.getDocument_id();
                    scores.putIfAbsent(doc_id, 0.0);
                    scores.put(doc_id, scores.get(doc_id) +  post.getWeight());
                }
            }
        }

        Map<String, Double> docs_boost = boostRankWithPositions(query, match_score, margin_window_error, penalization_score);

        //System.out.println("Query : " + query);
        //System.out.println("Sem ");
        //System.out.println(sortMapByValue(scores, n_top_docs));
        //System.out.println("POsitiomns");

        // TODO: maybe need function to smooth the results | check if doc_boost exist in scores
        for(String doc_id : docs_boost.keySet())
            scores.put(doc_id, scores.get(doc_id) + Math.log10(docs_boost.get(doc_id)));

        //System.out.println(sortMapByValue(scores, n_top_docs));
        return sortMapByValue(scores, n_top_docs);
    }

    private Map<String, Double> boostRankWithPositions(Map<String, Post> query, double match_score, int margin_window_error,
                                                                                            double penalization_score){

        String [] tokens_query = query.keySet().toArray(new String[0]);
        Map<String, Double> rank_docs = new HashMap<>();

        for(int i = 0; i < tokens_query.length ; i++){

            for(Post post : inverted_index.get(tokens_query[i])){

                // check that the document has been visited
                if (!rank_docs.containsKey( post.getDocument_id() )){
                    for(int pos : post.getPositions()){

                        // size of window
                        int initial_pos = pos - margin_window_error;
                        int end_pos =  pos + query.size() - i + margin_window_error;

                        for( int j = i + 1; j < tokens_query.length ; j++){

                            Post temp_post = new Post( post.getDocument_id() , 0);

                            int expected_pos_other_token = pos + j;

                            if (inverted_index.get(tokens_query[j]).contains(temp_post)){

                                int index = inverted_index.get(tokens_query[j]).indexOf(temp_post);
                                List<Integer> positions_other_token = inverted_index.get(tokens_query[j])
                                                                        .get(index).getPositions();

                                int pos_other_token = closestNumberOnList(expected_pos_other_token, positions_other_token);

                                double score = 0.0;

                                // check if value is within the window
                                if (initial_pos > pos_other_token || pos_other_token > end_pos)
                                    continue;

                                // match
                                else if( pos_other_token == expected_pos_other_token)
                                    score = match_score;

                                // penalization by pos TODO: maybe dont need else
                                else{
                                    score = match_score - (Math.abs(pos_other_token - expected_pos_other_token)
                                            * penalization_score);
                                }
                                // save boost score
                                if(!rank_docs.containsKey(post.getDocument_id()))
                                    rank_docs.put(post.getDocument_id(), 0.0);

                                // using max function to avoid penalties (only boost docs)
                                rank_docs.put(post.getDocument_id(), Math.max(rank_docs.get(post.getDocument_id()) + score, 0));
                            }
                        }
                    }
                }

            }
        }
        return sortMapByValue(rank_docs);
    }

    private Map<String, Double> sortMapByValue(Map<String, Double> map_to_sort){
        Map<String, Double> top_scores = new LinkedHashMap<>();

        map_to_sort.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(x -> top_scores.put(x.getKey(), x.getValue()));

        return  top_scores.entrySet()
                .stream()
                .collect(LinkedHashMap::new,(m,v) -> m.put(v.getKey(), v.getValue()), Map::putAll );
    }

    private Map<String, Integer> sortMapByValueInteger(Map<String, Integer> map_to_sort){
        Map<String, Integer> top_scores = new LinkedHashMap<>();

        map_to_sort.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                .forEach(x -> top_scores.put(x.getKey(), x.getValue()));

        return  top_scores.entrySet()
                .stream()
                .collect(LinkedHashMap::new,(m,v) -> m.put(v.getKey(), v.getValue()), Map::putAll );
    }

    private Map<String, Double> sortMapByValue(Map<String, Double> map_to_sort, int n_top_docs){
        Map<String, Double> top_scores = new LinkedHashMap<>();

        map_to_sort.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(x -> top_scores.put(x.getKey(), x.getValue()));

        return  top_scores.entrySet()
                .stream()
                .limit(n_top_docs)
                .collect(LinkedHashMap::new,(m,v) -> m.put(v.getKey(), v.getValue()), Map::putAll );
    }

    private int closestNumberOnList(int my_number, List<Integer> numbers){

        int distance = Math.abs(numbers.get(0) - my_number);
        int idx = 0;

        for(int c = 1; c < numbers.size(); c++){

            int new_distance = Math.abs(numbers.get(c) - my_number);

            if(new_distance < distance){
                idx = c;
                distance = new_distance;
            }
        }

        return numbers.get(idx);
    }


    //TODO: can need be removed
    private void loadQueries(String name_file){

    }

    public Map<String, List<Post>> getInverted_index() {
        return inverted_index;
    }
}
