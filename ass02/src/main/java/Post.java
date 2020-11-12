import java.io.PipedOutputStream;
import java.util.*;

public class Post implements Comparable<Post>{

    private int document_id;
    private int freqToken;
    private double weight;


    public Post(int document_id){
        this.document_id = document_id;
        this.freqToken = 1;
    }


    private double log2(int number){
        return Math.log(number) / Math.log(2);
    }


    public int getDocument_id() { return document_id; }


    public double getWeight(){ return weight; }


    public void increaseFreq(){ freqToken++; }


    public void setDocument_id(int doc_id){
        this.document_id = doc_id;
    }


    public int getFreqToken(){ return freqToken; }


    public void tfIdfWeighting(int N, int df){

        // W = (1 + log2(TF)) * log10(N/df)
        weight = (1 + log2(freqToken)) * Math.log10((double) N/df); //TODO: can need be convert to double
    }


    // compare only document id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return document_id == post.document_id;
    }


    @Override
    public int hashCode() {
        return Objects.hash(document_id);
    }


    // TODO: can dont be necessary
    @Override
    public int compareTo(Post p) {
        return (freqToken >= p.freqToken) ? 1 : -1;
    }


    @Override
    public String toString() {
        return "Post{" +
                "document_id=" + document_id +
                ", freqToken=" + freqToken +
                ", weight=" + weight +
                '}';
    }
}
