import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.*;


/*
Run as follows:
    - compile:
        mvn compile
    - run:
        mvn exec:java -Dexec.args="data/all_sources_metadata_2020-03-13.csv data/snowball_stopwords_EN.txt"
 */

public class App {


    public static void main(String[] args) throws IOException {
        /*
        Application may receive exactly 2 arguments, in the order that follows:
            - path of data set file
            - path of stop words file
        Default files are used if no input is given.
         */

        String csv_file = "data/metadata_2020-03-27.csv";
        String stop_words_file = "data/snowball_stopwords_EN.txt";

        if (args.length == 2) {
            csv_file = args[0];
            stop_words_file = args[1];
        }

        // (Un)comment to choose Tokenizer
        //Tokenizer tokenizer = new ImprovedTokenizer(stop_words_file);
        Tokenizer tokenizer = new SimpleTokenizer();


        double b = 0.75;
        double k = 1.2;

        //pipeline_indexer_tfidf(csv_file, tokenizer);
        //pipeline_searching_tfidf(tokenizer, "coronavirus origin", 50);

        //pipeline_indexer_bm25(csv_file, tokenizer, b, k);
        //pipeline_searching_BM25(tokenizer, "coronavirus origin", 50);


        /*
        CALCULATE THE STATISTICS
         */

        // toggle statistic calculation
        if(false)
            System.exit(-1);

        // Queries Solutions
        File my_file = new File("data/queries.relevance.filtered.txt");
        Scanner myReader = new Scanner(my_file);

        Map<String, Map<String, Integer>> queries_solutions = new HashMap<>();

        while (myReader.hasNextLine()) {
            String[] line = myReader.nextLine().split(" ");
            queries_solutions.putIfAbsent(line[0], new HashMap<>());
            queries_solutions.get(line[0]).put(line[1], Integer.parseInt(line[2]));
        }

        System.out.println("Solutions size: " + queries_solutions.size());


        // Sort queries solutions by relevance to calculate NDCG
        for (String main_key : queries_solutions.keySet()){
            Map<String, Integer> temp_map = new LinkedHashMap<>();

            queries_solutions.get(main_key).entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .forEach(x -> temp_map.put(x.getKey(), x.getValue()));
            queries_solutions.put(main_key, temp_map);

        }

        // 50 Queries
        File queries_file = new File("data/queries.txt");
        Scanner queries_reader = new Scanner(queries_file);

        int top_count ;
        int true_positives;
        double sum_precision ;
        double sum_dcg;
        double ideal_dcg;
        int n_top_docs = 50;

        Searcher s = new Searcher("resultsTfIdf.txt", tokenizer);
        //Searcher s = new Searcher("resultsBM25.txt", tokenizer);


        Map<String, ResultsInformation> results_rank10 = new HashMap<>();
        Map<String, ResultsInformation> results_rank20 = new HashMap<>();
        Map<String, ResultsInformation> results_rank50 = new HashMap<>();

        int lines_read = queries_solutions.size();
        double[] query_latency = new double[lines_read];

        for (int l = 1; l <= lines_read; l++) {
            top_count = 0;
            true_positives = 0;
            sum_precision = 0.0;
            sum_dcg = 0;
            ideal_dcg = 0;

            String query = queries_reader.nextLine();

            // Query latency
            final long startTime = System.nanoTime();
            Map<String, Double> scores = s.searchingLncLtc(query, n_top_docs);
            //Map<String, Double> scores = s.searchingBM25(query, n_top_docs);
            query_latency[l - 1] = (System.nanoTime() - startTime) / (Math.pow(10, 12));

            // DCG perfect ranking order
            Set<String> ideal_order = queries_solutions.get(l + "").keySet();
            Iterator<String> ideal_iterator = ideal_order.iterator();


            for (String key : scores.keySet()) {

                top_count++;
                double relevance = queries_solutions.get(l + "").getOrDefault(key, 0);

                if (top_count == 1){
                    sum_dcg += relevance;
                    ideal_dcg += queries_solutions.get(l + "").get(ideal_iterator.next());
                }
                else{
                    double v = Math.log(top_count) / Math.log(2);
                    sum_dcg += (relevance / v);
                    ideal_dcg += ideal_iterator.hasNext() ?  (queries_solutions.get(l + "").get(ideal_iterator.next()) / v) : 0;
                }

                if (queries_solutions.get(l + "").containsKey(key)) {
                    true_positives++;
                    sum_precision += ((double) true_positives / top_count);
                }

                if (top_count == 10) {
                    results_rank10.put(l + "", new ResultsInformation(sum_precision / true_positives, (double) true_positives / top_count, (double) true_positives / queries_solutions.get("1").size(), sum_dcg / ideal_dcg));
                }
                else if (top_count == 20) {
                    results_rank20.put(l + "", new ResultsInformation(sum_precision / true_positives, (double) true_positives / top_count, (double) true_positives / queries_solutions.get("1").size(), sum_dcg / ideal_dcg));
                }
                else if (top_count == 50) {
                    results_rank50.put(l + "", new ResultsInformation(sum_precision / true_positives, (double) true_positives / top_count, (double) true_positives / queries_solutions.get("1").size(), sum_dcg / ideal_dcg));
                    break;
                }
            }
        }

        Arrays.sort(query_latency);
        double median;

        if (query_latency.length % 2 == 0)
            median = (query_latency[query_latency.length/2] + query_latency[query_latency.length/ 2 - 1])/2;
        else
            median = query_latency[query_latency.length/2];

        for (int i = 1; i <= lines_read; i++) {
            System.out.println(results_rank10.get(i + ""));
            System.out.println(results_rank20.get(i + ""));
            System.out.println(results_rank50.get(i + ""));
            System.out.println("");
        }

        System.out.println("Query Latency (Median): " + median + " ms");
    }

    public static void pipeline_indexer_tfidf(String csv_file, Tokenizer tokenizer)throws IOException{

        CorpusReader corpusReader = new CorpusReader(csv_file);
        Indexer indexer = new IndexerTfIdf(corpusReader, tokenizer);

        // indexing
        final long startTime = System.nanoTime();
        indexer.process_index();
        final long endTime = System.nanoTime();

        System.out.println( "Time to indexing: " + (endTime - startTime) / Math.pow(10,9) + "s;" );

        // write in file the inverted index
        indexer.writeInFile("resultsTfIdf.txt");
    }

    public static void pipeline_indexer_bm25(String csv_file, Tokenizer tokenizer,double b,double k)throws IOException{

        CorpusReader corpusReader = new CorpusReader(csv_file);
        Indexer indexer = new IndexerBM25(corpusReader, tokenizer, k, b);

        // indexing
        final long startTime = System.nanoTime();
        indexer.process_index();
        final long endTime = System.nanoTime();

        System.out.println( "Time to indexing: " + (endTime - startTime) / Math.pow(10,9) + " seconds;" );

        // write in file the inverted index
        indexer.writeInFile("resultsBM25.txt");
    }

    public static void pipeline_searching_tfidf(Tokenizer tokenizer, String input, int n_top_docs) throws FileNotFoundException {

        Searcher s = new Searcher("resultsTfIdf.txt", tokenizer);

        final long startTime = System.nanoTime();
        Map<String, Double> scores =  s.searchingLncLtc(input, n_top_docs);
        final long endTime = System.nanoTime();

        System.out.println("Tf-IDF");
        System.out.println("Searching: " + input);
        System.out.println("Time to search: "+ (endTime - startTime) / Math.pow(10,9) + "s");
        System.out.println("Top " + n_top_docs + ": ");
        System.out.println("Docs        Score");
        for(String doc_id: scores.keySet()){
            System.out.println(doc_id + " -> " +scores.get(doc_id));
        }

    }

    public static void pipeline_searching_BM25(Tokenizer tokenizer, String input, int n_top_docs) throws FileNotFoundException {

        Searcher s = new Searcher("resultsBM25.txt", tokenizer);

        final long startTime = System.nanoTime();
        Map<String, Double> scores =  s.searchingBM25(input, n_top_docs);
        final long endTime = System.nanoTime();

        System.out.println("BM25");
        System.out.println("Searching: " + input);
        System.out.println("Time to search: "+ (endTime - startTime) / Math.pow(10,9) + "s");
        System.out.println("Top " + n_top_docs + ": ");
        System.out.println("Docs        Score");
        for(String doc_id: scores.keySet()){
            System.out.println(doc_id + " -> " +scores.get(doc_id));
        }

    }
}


