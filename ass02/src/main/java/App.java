import java.io.IOException;
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

//        CorpusReader corpusReader = new CorpusReader(csv_file);
//
//        // (Un)comment to choose Tokenizer
        Tokenizer tokenizer = new ImprovedTokenizer(stop_words_file);
////        Tokenizer tokenizer = new SimpleTokenizer();
////        IndexerTfIdf indexer_a;
//        IndexerBM25 indexer_b;
//
//        //TODO: Through from command line
//        double b = 0.75;
//        double k = 1.2;
//
//
////        indexer_a = new IndexerTfIdf(corpusReader, tokenizer);
//        indexer_b = new IndexerBM25(corpusReader, tokenizer, k, b);
//
//        // indexing
//        final long startTime = System.nanoTime();
////        Map<String, List<Post>> inverted_index = indexer_a.process_index();
//        Map<String, List<Post>> inverted_index = indexer_b.process_index();
//        final long endTime = System.nanoTime();
//        System.out.println(inverted_index.size());

//        indexer_a.writeInFile("results.txt");
//        indexer_b.writeInFile("results.txt");

        Searcher s = new Searcher("results.txt", tokenizer);
        Map<String, List<Post>> inverted_index = s.getInverted_index();
//
        Map<String, Double> scores =  s.searchingLncLtc("coronavirus origin");
//
        System.out.println("Search: coronavirus origin");
        for(String key :scores.keySet())
          System.out.println(key+" -> " + scores.get(key));


        //for(String key : inverted_index.keySet())
          //  System.out.println(key+" -> " + inverted_index.get(key));

        //System.out.println("size of list " +inverted_index.get("2020").size());
        //System.out.println(inverted_index.get("2020"));


       /*
       This code is used merely to answer the questions.
       Therefore efficiency was not taken into account.
        */

//        System.out.println("a) Time to indexing: " + (endTime - startTime));
        //System.out.println("b) Vocabulary size: " + inverted_index.size());


       //int i = 0;
       //System.out.println("c)");
       //for(String token: inverted_index.keySet()){
       //     if(inverted_index.get(token).size() == 1){
       //         System.out.println(token + " : " + inverted_index.get(token));
       //         i++;
       //     }
       //     if(i>10)
       //         break;
       //}

        //Map<String, Integer> freq = new TreeMap<>();
//
        //for(String x : inverted_index.keySet())
        //    freq.put(x, inverted_index.get(x).size());
//
        //freq.entrySet()
        //        .stream()
        //        .sorted(Map.Entry.comparingByValue())
        //        .forEach(System.out::println);


    }
}


