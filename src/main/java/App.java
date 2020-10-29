import com.sun.source.tree.Tree;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toMap;

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

        String csv_file = "data/all_sources_metadata_2020-03-13.csv";
        String stop_words_file = "data/snowball_stopwords_EN.txt";

        if (args.length == 2) {
            csv_file = args[0];
            stop_words_file = args[1];
        }

        CorpusReader corpusReader = new CorpusReader();

        // (Un)comment to choose Tokenizer
        // Tokenizer tokenizer = new ImprovedTokenizer(stop_words_file);
        Tokenizer tokenizer = new SimpleTokenizer();
        Indexer indexer;

        corpusReader.loadDataCSV(csv_file);

        indexer = new Indexer(corpusReader.getDocuments(), tokenizer);


        // indexing
        final long startTime = System.nanoTime();
        Map<String, Set<Integer>> inverted_index = indexer.process_index();
        final long endTime = System.nanoTime();


        //ByteArrayOutputStream baos=new ByteArrayOutputStream();
        //ObjectOutputStream oos=new ObjectOutputStream(baos);
        //oos.writeObject(inverted_index);
        //oos.close();
        //System.out.println("Data Size: " + baos.size() );


       System.out.println("a) Time to indexing: " + (endTime - startTime));
       System.out.println("b) Vocabulary size: " + inverted_index.size());


       /*
       This code is used merely to answer the questions.
       Therefore efficiency was not taken into account.
        */

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


