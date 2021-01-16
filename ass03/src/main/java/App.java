import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;


/*
André Alves - 89334
Luís Fonseca - 89066

Run as follows:
    - compile:
        mvn compile
    - run:
        mvn exec:java -Dexec.args="data/metadata_2020-03-27.csv data/snowball_stopwords_EN.txt"
        OR
        mvn exec:java
    - run (for statistics):
         mvn exec:java -Dexec.args="data/metadata_2020-03-27.csv data/snowball_stopwords_EN.txt" > <nameOfFile>
         OR
         mvn exec:java > <nameOfFile>
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

        /* from project03 */
        boolean with_boost = true;
        int memory_mb_max = 1000;
        int merges_at_the_time = 2;
        boolean with_positions = true;

        if (args.length == 6) {
            csv_file = args[0];
            stop_words_file = args[1];
            with_boost = Boolean.parseBoolean(args[2]);
            memory_mb_max = Integer.parseInt(args[3]);
            merges_at_the_time = Integer.parseInt(args[4]);
            with_positions = Boolean.parseBoolean(args[5]);
        }

        // CHANGE to choose Tokenizer
        //Tokenizer tokenizer = new ImprovedTokenizer(stop_words_file);
        Tokenizer tokenizer = new SimpleTokenizer();

        double b = 0.75;
        double k = 1.2;



        /* Without Merge (From Project02)*/
//        pipeline_indexer_tfidf(csv_file, tokenizer, with_positions);                  // (writes to a file (needed for the statistics part))
//        pipeline_searching_tfidf(tokenizer, "coronavirus origin", 50);

//        pipeline_indexer_bm25(csv_file, tokenizer, b, k, with_positions);             // (writes to a file (needed for the statistics part))
//        pipeline_searching_BM25(tokenizer, "coronavirus response to weather changes", 50);


        /* With Merge*/
//        pipeline_indexer_tfidf_w_merge(csv_file, tokenizer, memory_mb_max, merges_at_the_time);
        pipeline_indexer_bm25_w_merge(csv_file, tokenizer, b, k, memory_mb_max, merges_at_the_time);

        /*
        *
        CALCULATE THE STATISTICS
        *
        */

        // CHANGE statistic calculation
        if(true)
            System.exit(-1);

        /*  Search (CHANGE) */
        /*   Without Merge  (from project02)      */
        // CHANGE in accordance to the file name
        //Searcher s = new Searcher("resultsTfIdf.txt", tokenizer);
//        Searcher s = new Searcher("resultsBM25.txt", tokenizer);

        /* With Merge*/
        Searcher s = new Searcher(tokenizer, "index_split", memory_mb_max);

        // Queries Solutions
        File my_file = new File("data/queries.relevance.filtered.txt");
        Scanner myReader = new Scanner(my_file);

        Map<String, Map<String, Integer>> queries_solutions = new HashMap<>();

        while (myReader.hasNextLine()) {
            String[] line = myReader.nextLine().split(" ");
            queries_solutions.putIfAbsent(line[0], new HashMap<>());
            queries_solutions.get(line[0]).put(line[1], Integer.parseInt(line[2]));
        }

//        System.out.println("Solutions size: " + queries_solutions.size());

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

        Map<String, ResultsInformation> results_rank10 = new HashMap<>();
        Map<String, ResultsInformation> results_rank20 = new HashMap<>();
        Map<String, ResultsInformation> results_rank50 = new HashMap<>();

        int queries_read = queries_solutions.size();
        double[] query_latency = new double[queries_read];

        for (int l = 1; l <= queries_read; l++) {

            top_count = 0;
            true_positives = 0;
            sum_precision = 0.0;
            sum_dcg = 0.0;
            ideal_dcg = 0.0;

            String query = queries_reader.nextLine();

            // Query latency
            final long startTime = System.nanoTime();

            
            /*  Search (CHANGE) */
            /*  (from project02)      */
            // CHANGE in accordance to the file name
            //Map<String, Double> scores = searchingLncLtcWithoutPositions(query, n_top_docs);
            //Map<String, Double> scores = searchingBM25WithoutPositions(query, n_top_docs)

            /*   (from project03)      */
            //Map<String, Double> scores = s.searchingBM25WithPositions(query, n_top_docs, with_boost, 500, 5, 50);
            Map<String, Double> scores = s.searchingLncLtcWithPositions(query, n_top_docs, with_boost,  100, 2, 5);
            query_latency[l - 1] = (System.nanoTime() - startTime) / (Math.pow(10, 6));

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
                    double ndcg = check_division_by_zero(sum_dcg , ideal_dcg);
                    double avg_precision = check_division_by_zero(sum_precision, true_positives);
                    results_rank10.put(l + "", new ResultsInformation(avg_precision, (double) true_positives / top_count, (double) true_positives / queries_solutions.get(l + "").size(), ndcg));
                }
                else if (top_count == 20) {
                    double ndcg = check_division_by_zero(sum_dcg , ideal_dcg);
                    double avg_precision = check_division_by_zero(sum_precision, true_positives);
                    results_rank20.put(l + "", new ResultsInformation(avg_precision, (double) true_positives / top_count, (double) true_positives / queries_solutions.get(l + "").size(), ndcg));
                }
                else if (top_count == 50) {
                    double ndcg = check_division_by_zero(sum_dcg , ideal_dcg);
                    double avg_precision = check_division_by_zero(sum_precision, true_positives);
                    results_rank50.put(l + "", new ResultsInformation(avg_precision, (double) true_positives / top_count, (double) true_positives / queries_solutions.get(l + "").size(), ndcg));
                    break;
                }
            }
        }

        double[] query_latency_copy = query_latency.clone();
        
        Arrays.sort(query_latency);
        double median;

        if (query_latency.length % 2 == 0)
            median = (query_latency[query_latency.length/2] + query_latency[query_latency.length/ 2 - 1])/2;
        else
            median = query_latency[query_latency.length/2];

        System.out.println("Query Latency (Median): " + median + " ms");
        System.out.println("Query throughput " + ( queries_read / ((Arrays.stream(query_latency).sum()) * Math.pow(10,-3))));

        /*
        * TABLE CONSTRUCTION
        *
        * */

        String[] measures = new String[]{
                "", "Precision", "Recall", "F-measure", "Avg Precision", "NDCG", "\tLatency(ms)"};

        // MAIN HEADER
        for (String m : measures) {
            if (m.isEmpty()) {
                System.out.printf("%10s", "");
            } else {
                System.out.printf("%21s", m);
            }
        }
        System.out.println();

        // SECONDARY HEADER
        for (int i = 0; i < measures.length; i++) {
            if (i == 0) {
                System.out.printf("%10s", "Query #");
            } else if (i == measures.length - 1) {
                System.out.printf("%21s", "");
            } else {
                System.out.printf("%7s %7s %7s", "@10", "@20", "@30");
            }
        }
        System.out.println();
        for (int line = 1; line <= queries_read; line++) {

            // RESULTS
            for (int i = 0; i < measures.length; i++) {
                if (i == 0) {
                    System.out.printf("%10d", line);
                } else if (i == measures.length - 1) {
                    System.out.printf("%21.2f", query_latency_copy[line - 1]);
                } else {
                    double value01;
                    double value02;
                    double value03;
                    switch (i) {
                        case 1:
                            value01 = results_rank10.get(line + "").getPrecision();
                            value02 = results_rank20.get(line + "").getPrecision();
                            value03 = results_rank50.get(line + "").getPrecision();
                            break;
                        case 2:
                            value01 = results_rank10.get(line + "").getRecall();
                            value02 = results_rank20.get(line + "").getRecall();
                            value03 = results_rank50.get(line + "").getRecall();
                            break;
                        case 3:
                            value01 = results_rank10.get(line + "").getF1();
                            value02 = results_rank20.get(line + "").getF1();
                            value03 = results_rank50.get(line + "").getF1();
                            break;
                        case 4:
                            value01 = results_rank10.get(line + "").getAvg_precision();
                            value02 = results_rank20.get(line + "").getAvg_precision();
                            value03 = results_rank50.get(line + "").getAvg_precision();
                            break;
                        case 5:
                            value01 = results_rank10.get(line + "").getNdcg();
                            value02 = results_rank20.get(line + "").getNdcg();
                            value03 = results_rank50.get(line + "").getNdcg();
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + i);
                    }

                    System.out.printf("%7.3f %7.3f %7.3f",
                            value01,
                            value02,
                            value03);
                }
            }

            System.out.println();

        }

        // MEAN
        for (int i = 0; i < measures.length; i++) {
            if (i == 0) {
                System.out.printf("%10s", "Mean");
            } else if (i == measures.length - 1) {
                System.out.printf("%21.2f", Arrays.stream(query_latency).sum() / query_latency.length);
            } else {
                double value01 = 0;
                double value02 = 0;
                double value03 = 0;
                switch (i) {
                    case 1:
                        for (int q = 1; q <= queries_read; q++) {
                            value01 += results_rank10.get(q + "").getPrecision();
                            value02 += results_rank20.get(q + "").getPrecision();
                            value03 += results_rank50.get(q + "").getPrecision();
                        }
                        break;
                    case 2:
                        for (int q = 1; q <= queries_read; q++) {
                            value01 += results_rank10.get(q + "").getRecall();
                            value02 += results_rank20.get(q + "").getRecall();
                            value03 += results_rank50.get(q + "").getRecall();
                        }
                        break;
                    case 3:
                        for (int q = 1; q <= queries_read; q++) {
                            value01 += results_rank10.get(q + "").getF1();
                            value02 += results_rank20.get(q + "").getF1();
                            value03 += results_rank50.get(q + "").getF1();
                        }
                        break;
                    case 4:
                        for (int q = 1; q <= queries_read; q++) {
                            value01 += results_rank10.get(q + "").getAvg_precision();
                            value02 += results_rank20.get(q + "").getAvg_precision();
                            value03 += results_rank50.get(q + "").getAvg_precision();
                        }
                        break;
                    case 5:
                        for (int q = 1; q <= queries_read; q++) {
                            value01 += results_rank10.get(q + "").getNdcg();
                            value02 += results_rank20.get(q + "").getNdcg();
                            value03 += results_rank50.get(q + "").getNdcg();
                        }
                        break;
                }
                System.out.printf("%7.3f %7.3f %7.3f",
                        value01 / (queries_read),
                        value02 / (queries_read),
                        value03 / (queries_read));
            }
        }
        System.out.println();
    }

    public static double check_division_by_zero(double a, double b){
        if(b == 0)
            return 0;
        return a/b;
    }

    public static void pipeline_indexer_tfidf(String csv_file, Tokenizer tokenizer, boolean withpos)throws IOException{

        CorpusReader corpusReader = new CorpusReader(csv_file);
        Indexer indexer = new IndexerTfIdf(corpusReader, tokenizer);

        // indexing
        final long startTime = System.nanoTime();
        Map<String, List<Post>> inverted_index = indexer.processIndexWithPositions();
        final long endTime = System.nanoTime();

        System.out.println( "Time to indexing: " + (endTime - startTime) / Math.pow(10,9) + "s;" );
//        System.out.println("N: " + indexer.N);

        // write in file the inverted index
        indexer.writeResultsInDisk(withpos, "resultsTfIdf.txt");
    }

    public static void pipeline_indexer_tfidf_w_merge(String csv_file, Tokenizer tokenizer, int memory_mb_max, int merges_at_the_time)throws IOException{

        CorpusReader corpusReader = new CorpusReader(csv_file);
        Indexer indexer = new IndexerTfIdf(corpusReader, tokenizer);
        String merge_file_name = "final_merge_tfidf";

        // indexing
        final long startTime = System.nanoTime();

        indexer.processIndexWithMerge(merge_file_name, memory_mb_max);
        indexer.mergeFiles(merge_file_name, memory_mb_max, merges_at_the_time);
        indexer.splitMergedFile(merge_file_name , memory_mb_max);

        final long endTime = System.nanoTime();

        System.out.println( "Time to indexing: " + (endTime - startTime) / Math.pow(10,9) + "s;" );
    }


    public static void pipeline_indexer_bm25(String csv_file, Tokenizer tokenizer,double b,double k, boolean withpos)throws IOException{

        CorpusReader corpusReader = new CorpusReader(csv_file);
        Indexer indexer = new IndexerBM25(corpusReader, tokenizer, k, b);

        // indexing
        final long startTime = System.nanoTime();
        Map<String, List<Post>> inverted_index = indexer.processIndexWithPositions();
        final long endTime = System.nanoTime();

        System.out.println( "Time to indexing: " + (endTime - startTime) / Math.pow(10,9) + " seconds;" );

        // write in file the inverted index
        indexer.writeResultsInDisk(withpos, "resultsBM25.txt");
    }

    public static void pipeline_indexer_bm25_w_merge(String csv_file, Tokenizer tokenizer ,double b, double k, int memory_mb_max, int merges_at_the_time) throws IOException {
        CorpusReader corpusReader = new CorpusReader(csv_file);
        Indexer indexer = new IndexerBM25(corpusReader, tokenizer, k, b);
        String merge_file_name = "final_merge_bm25";

        // indexing
        final long startTime = System.nanoTime();
        indexer.processIndexWithMerge(merge_file_name, memory_mb_max);
        indexer.mergeFiles(merge_file_name, memory_mb_max, merges_at_the_time);
        indexer.splitMergedFile(merge_file_name , memory_mb_max);
        final long endTime = System.nanoTime();

        System.out.println( "Time to indexing: " + (endTime - startTime) / Math.pow(10,9) + " seconds;" );
    }

    public static void pipeline_searching_tfidf(Tokenizer tokenizer, String input, int n_top_docs) throws FileNotFoundException {

        Searcher s = new Searcher("resultsTfIdf.txt", tokenizer);

        final long startTime = System.nanoTime();
        Map<String, Double> scores =  s.searchingLncLtcWithoutPositions(input, n_top_docs);
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
        Map<String, Double> scores =  s.searchingBM25WithoutPositions(input, n_top_docs);
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


