import java.util.*;

public class Teste {
    public static void main(String[] args) {

        //TODO: delete this file before to submit the work


        Set<Post> x = new TreeSet<>();

        Post p1 = new Post(1);
        Post p2 = new Post(1);

        x.add(p1);

        System.out.println( x.contains(p2) );

        // count freq
       // x.get( x.indexOf(p2) ).increaseFreq();

        System.out.println( p1.getFreqToken() ) ;


        //System.out.println( p1.getDocument_id() );
        //System.out.println( p2.getDocument_id() );


    }
}
