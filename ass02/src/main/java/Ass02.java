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

public class Ass02 {

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

//        CorpusReader corpusReader = new CorpusReader(csv_file);

        // (Un)comment to choose Tokenizer
        Tokenizer tokenizer = new ImprovedTokenizer(stop_words_file);
        //Tokenizer tokenizer = new SimpleTokenizer();
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


//        Searcher s = new Searcher("resultsBM25.txt", tokenizer);
        Searcher s = new Searcher("results.txt", tokenizer);
        ////Map<String, List<Post>> inverted_index = s.getInverted_indexer();

//        Map<String, Double> scores =  s.searchingBM25("coronavirus origin");
        Map<String, Double> scores =  s.searchingLncLtc("coronavirus origin");

        //
        System.out.println("Search: coronavirus origin");
        for(String key :scores.keySet())
            System.out.println(key+" -> " + scores.get(key));
//
        System.out.println("> " + scores.size());


        File my_file = new File("data/queries.relevance.filtered.txt");
        Scanner myReader = new Scanner(my_file);

        Map<String, List<String>> queries_solutions = new HashMap<>();

        while (myReader.hasNextLine()){
            String[] line = myReader.nextLine().split(" ");
            queries_solutions.putIfAbsent(line[0], new ArrayList<>());
            queries_solutions.get(line[0]).add(line[1]);
        }

        System.out.println("queries size: " + queries_solutions.size());
        System.out.println(queries_solutions.get("1").size());

//        for(int i = 0; i < queries_solutions.get("1").size(); i++){
//            if (scores.get(queries_solutions.get("1").get(i)) == null){
//                System.out.println("F - " + queries_solutions.get("1").get(i));
//            } else {
//                System.out.println("R - " + queries_solutions.get("1").get(i));
//            }
//        }

        int top_count = 0;
        int true_positives = 0;
        List<Double> precision_list = new ArrayList<>();
        List<Double> recall_list = new ArrayList<>();
        for(String key :scores.keySet()){
            top_count++;
            if (queries_solutions.get("1").contains(key)){
                true_positives++;
                precision_list.add( ( (double) true_positives / top_count));
                recall_list.add(( (double) true_positives / queries_solutions.size()));
            }
        }
        for (int i = 0; i < precision_list.size(); i++) {
            System.out.println("P= " + precision_list.get(i));
            System.out.println("R= " + recall_list.get(i));
        }
        System.out.println(top_count);
        System.out.println(true_positives);

    }
}


