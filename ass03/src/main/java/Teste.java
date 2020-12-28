import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Teste {
    public static void main(String[] args) throws IOException {

        Map<String, List<Post>> indexer = new TreeMap<>();
        Map<String, List<Post>> indexerb = new TreeMap<>();
        Post post1 = new Post("doc1", 2, Arrays.asList(23, 27) );
        Post post2 = new Post("doc2", 2, Arrays.asList(2, 27) );
        Post post3 = new Post("doc1", 1, Arrays.asList(14, 17) );
        Post post4 = new Post("doc4", 4, Arrays.asList(30, 50) );
        Post post5 = new Post("doc4", 7, Arrays.asList(34, 35) );
        Post post6 = new Post("doc3", 6, Arrays.asList(34, 38) );

        indexer.put("ola", new ArrayList<>(Arrays.asList(post1, post2)));
        indexer.put("adeus", new ArrayList<>(Arrays.asList(post3, post4)));


        indexerb.put("adeus", new ArrayList<>(Arrays.asList(post5, post6)));


        Map<String, List<Post>> indexerc = merge(indexer, indexerb);

        for(String a : indexerc.keySet())
            System.out.println(a + " - " + indexerc.get(a));
    }

    public static Map<String, List<Post>> merge(Map<String, List<Post>> A_doc, Map<String, List<Post>> B_doc){

        Map<String, List<Post>> temp_indexer = new TreeMap<>();

        for(String term : A_doc.keySet()){

            temp_indexer.put(term, A_doc.get(term));

            if(B_doc.containsKey(term))
                temp_indexer.get(term).addAll(B_doc.get(term));
        }

        for(String term : B_doc.keySet())
            if(!temp_indexer.containsKey(term))
                temp_indexer.put(term, B_doc.get(term));

        return temp_indexer;
    }
}
