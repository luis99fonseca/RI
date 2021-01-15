import java.util.*;

public class Post implements Comparable<Post>{

    private String document_id;
    private int freqToken;
    private double weight = 0.0;
    private List<Integer> positions = new ArrayList<>();

    private int length = 0;

    public Post(){

    }
    //    Merge TfIdf Constructor
    public Post(String document_id, double weight, List<Integer> positions){
        this.document_id = document_id;
        this.weight = weight;
        this.positions = positions;
    }

    public Post(String document_id, int freqToken, List<Integer> positions){
        this.document_id = document_id;
        this.freqToken = freqToken;
        this.positions = positions;
    }

    //    Merge Bm25 Constructor
    public Post(String document_id, List<Integer> positions, int docLen, int freqToken){
        this.document_id = document_id;
        this.positions = positions;
        this.length = docLen;
        this.freqToken = freqToken;
    }

    public Post(String document_id, double weight){
        this.document_id = document_id;
        this.weight = weight;
    }


    private double log2(int number){
        return Math.log(number) / Math.log(2);
    }


    public String getDocument_id() { return document_id; }

    public List<Integer> getPositions(){
        return positions;
    }


    public String getTextPositions(){
        StringBuilder pos = new StringBuilder();
        for(int num_pos : positions){
            String pos_common = num_pos + ",";
            pos.append(pos_common);
        }

        return pos.substring(0, pos.length()-1);
    }


    public double getWeight(){ return weight; }


    public void setWeight(double weight){
        this.weight = weight;
    }

    public int getLength() {
        return length;
    }

    public void increaseFreq(){ freqToken++; }

    public int getFreqToken() {
        return freqToken;
    }

    public void tfIdfWeighting(){

        // W = (1 + log10(TF)) * n
        weight = (1 + Math.log10(freqToken)) * 1;
    }

    public void tfIdfWeighting(double idf){
        weight = (1 + Math.log10(freqToken)) * idf;
    }

    public void BM25(double k, double b, double avdl, int dl, int N, int df){
//        System.out.println(k +"; " + b +"; " + "; " + avdl + "; " + dl + "; " + N + "; " + df);

        double idf = calIDF(N, df);
        weight = idf * ( ((k + 1) * freqToken) / (k * ( (1 - b) + b * dl/avdl ) + freqToken) );
//        System.out.println("Weight: " + weight);
    }

    private double calIDF(int N, int df){
        return Math.log10((double) N/df);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return document_id.equals(post.document_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(document_id);
    }


    @Override
    public int compareTo(Post p) {
        return (freqToken >= p.freqToken) ? 1 : -1;
    }


    @Override
    public String toString() {

        if (positions.isEmpty())
            return "Post{" +
                    "document_id=" + document_id +
                    ", freqToken=" + freqToken +
                    ", score=" + weight +
                    '}';

        return "Post{" +
                "document_id=" + document_id +
                ", freqToken=" + freqToken +
                ", score=" + weight +
                ", positions= " + getTextPositions() +
                '}';
    }
}
