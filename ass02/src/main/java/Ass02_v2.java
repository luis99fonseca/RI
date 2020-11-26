import java.io.File;
import java.io.IOException;
import java.util.*;


/*
Run as follows:
    - compile:
        mvn compile
    - run:
        mvn exec:java -Dexec.args="data/all_sources_metadata_2020-03-13.csv data/snowball_stopwords_EN.txt"
 */

public class Ass02_v2 {

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

        CorpusReader corpusReader = new CorpusReader(csv_file);

        // (Un)comment to choose Tokenizer
//        Tokenizer tokenizer = new ImprovedTokenizer(stop_words_file);
        Tokenizer tokenizer = new SimpleTokenizer();
//        Indexer indexer;

        //TODO: Through from command line
        double b = 0.75;
        double k = 1.2;


//        indexer = new IndexerTfIdf(corpusReader, tokenizer);
//        indexer = new IndexerBM25(corpusReader, tokenizer, k, b);

        // indexing
//        final long startTime = System.nanoTime();
//        Map<String, List<Post>> inverted_index = indexer.process_index();
//        final long endTime = System.nanoTime();
//        System.out.println(inverted_index.size());
//        System.out.println("Time: " + (endTime - startTime));
//
//        System.out.println("docs: " + indexer.N);

//        indexer.writeInFile("results.txt");
//        indexer.writeInFile("resultsBM25.txt");

        Searcher s = new Searcher("resultsBM25.txt", tokenizer);
//        Searcher s = new Searcher("results.txt", tokenizer);
        ////Map<String, List<Post>> inverted_index = s.getInverted_indexer();

////        Map<String, Double> scores =  s.searchingBM25("coronavirus origin");
//        Map<String, Double> scores =  s.searchingLncLtc("coronavirus origin");

///////////////////////////////////////7
        File my_file = new File("data/queries.relevance.filtered.txt");
        Scanner myReader = new Scanner(my_file);

//        Map<String, List<String>> queries_solutions = new HashMap<>();
        Map<String, Map<String, Integer>> queries_solutions = new HashMap<>();

        while (myReader.hasNextLine()) {
            String[] line = myReader.nextLine().split(" ");
            queries_solutions.putIfAbsent(line[0], new HashMap<>());
            queries_solutions.get(line[0]).put(line[1], Integer.parseInt(line[2]));
        }

        System.out.println("SOLUTIONS SIZE= " + queries_solutions.size());
        System.out.println("SOLUTIONS (1) SIZE= " + queries_solutions.get("1").size());

        Map<String, Integer> temp_map = new LinkedHashMap<>();
        queries_solutions.get("1").entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(x -> temp_map.put(x.getKey(), x.getValue()));
        for (String t_key : queries_solutions.get("1").keySet()){
            System.out.println(queries_solutions.get("1").get(t_key));
        }
        System.out.println("------------------------");
        queries_solutions.put("1", temp_map);
        for (String t_key : queries_solutions.get("1").keySet()){
            System.out.println(queries_solutions.get("1").get(t_key));
        }

        System.exit(-1);


        File queries_file = new File("data/queries.txt");
        Scanner queries_reader = new Scanner(queries_file);

        int top_count ;
        int true_positives;
        List<Double> precision_list = new ArrayList<>();
        double sum_precision ;
        List<Double> recall_list = new ArrayList<>();
        double sum_recall ;
        List<Double> f1_score = new ArrayList<>();

        double sum_scg;

        Map<String, ResultsInformation> results_rank10 = new HashMap<>();
        Map<String, ResultsInformation> results_rank20 = new HashMap<>();
        Map<String, ResultsInformation> results_rank50 = new HashMap<>();

//        double query_throughput = 0.0;
        int lines_read = 1; // TODO: ser mais dinamico??
        double query_throughput[] = new double[lines_read];
        for (int l = 1; l <= lines_read; l++) {
            top_count = 0;
            true_positives = 0;
            sum_precision = 0.0;
            sum_recall = 0.0;

            sum_scg = 0;

            String query = queries_reader.nextLine();
            final long startTime = System.nanoTime();
            Map<String, Double> scores = s.searchingLncLtc(query);
            query_throughput[l - 1] = (System.nanoTime() - startTime) / (Math.pow(10, 9));
            System.out.println("QUERY= " + query + "; RESULTS= " + scores.size());

            for (String key : scores.keySet()) {
                System.out.println(">> " + key + "; " + scores.get(key));
                top_count++;
                if (queries_solutions.get(l + "").containsKey(key)) {
                    true_positives++;
//                precision_list.add( ( (double) true_positives / top_count));
                    sum_precision += ((double) true_positives / top_count);

                    if (top_count == 1)
                        sum_scg += queries_solutions.get(l + "").get(key);
                    else                                                                // TODO: pensar qual é o "i"
                        sum_scg += (queries_solutions.get(l + "").get(key) / (Math.log(true_positives) / Math.log(2)) );
//                recall_list.add(( (double) true_positives / queries_solutions.get("1").size()));
//                    sum_recall += ((double) true_positives / queries_solutions.get("1").size());
                }
                if (top_count == 10) {
//                precision_list.add( sum_precision / true_positives );
//                recall_list.add( sum_recall / true_positives );
//                f1_score.add(  (2 * precision_list.get(precision_list.size()-1) * recall_list.get(recall_list.size()-1))
//                        / (precision_list.get(precision_list.size()-1) + recall_list.get(recall_list.size()-1))
//                );
                    results_rank10.put(l + "", new ResultsInformation(sum_precision / true_positives, (double) true_positives / top_count, (double) true_positives / queries_solutions.get("1").size()));
                }
                else if (top_count == 20) {
//                precision_list.add( sum_precision / true_positives );
//                recall_list.add( sum_recall / true_positives );
//                f1_score.add(  (2 * precision_list.get(precision_list.size()-1) * recall_list.get(recall_list.size()-1))
//                        / (precision_list.get(precision_list.size()-1) + recall_list.get(recall_list.size()-1))
//                );
                    results_rank20.put(l + "", new ResultsInformation(sum_precision / true_positives, (double) true_positives / top_count, (double) true_positives / queries_solutions.get("1").size()));
                }
                else if (top_count == 50) {
//                precision_list.add( sum_precision / true_positives );
//                recall_list.add( sum_recall / true_positives );
//                f1_score.add(  (2 * precision_list.get(precision_list.size()-1) * recall_list.get(recall_list.size()-1))
//                        / (precision_list.get(precision_list.size()-1) + recall_list.get(recall_list.size()-1))
//                );
                    results_rank50.put(l + "", new ResultsInformation(sum_precision / true_positives, (double) true_positives / top_count, (double) true_positives / queries_solutions.get("1").size()));
                break;
                }
            }
        }
        System.out.println("Query Throughput= " + (Arrays.stream(query_throughput).sum() / lines_read) + "; " + (lines_read / Arrays.stream(query_throughput).sum()));
        Arrays.sort(query_throughput);
        double median;
        if (query_throughput.length % 2 == 0)
            median = (query_throughput[query_throughput.length/2] + (double)query_throughput[query_throughput.length/2 - 1])/2;
        else
            median = query_throughput[query_throughput.length/2];
//        double median = ((double) query_throughput[query_throughput.length / 2] + (double) query_throughput[query_throughput.length / 2 - 1]) / 2;
        System.out.println("Query Latency (Median in Seconds)= " + median);

        for (int i = 1; i <= lines_read; i++) {
            System.out.println(results_rank10.get(i + ""));
            System.out.println(results_rank20.get(i + ""));
            System.out.println(results_rank50.get(i + ""));
            System.out.println("");
        }

    }
}


