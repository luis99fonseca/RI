import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class IndexerBM25 extends Indexer{

    Map<String, Integer> docs_len = new HashMap<>();
    long total_len_docs = 0;

    // parameters of BM25
    double k, b;


    public IndexerBM25(CorpusReader corpusReader, Tokenizer tokenizer, double k, double b) {
        super(corpusReader, tokenizer);
        this.k = k;
        this.b = b;
    }


    public Map<String, List<Post>> processIndexWithoutPositions() {
        Map<String, String> document_list;

        while ( !(document_list = corpusReader.readBlock()).isEmpty() ) {

            // counting the read documents to calculate idf
            N += document_list.size();

            for (String doc_id : document_list.keySet()) {
                Map<String, Integer> temp_freq_tokens = new HashMap<>();
                int count_tokens = 0;

                for (String token : tokenizer.process_tokens(document_list.get(doc_id))) {

                    if (!token.isEmpty()) {
                        count_tokens++;
                        temp_freq_tokens.putIfAbsent(token, 0);
                        temp_freq_tokens.put( token, temp_freq_tokens.get(token) + 1 );
                    }
                }

                for(String token: temp_freq_tokens.keySet()){
                    inverted_index.computeIfAbsent(token, k-> new ArrayList<>());
                    inverted_index.get(token).add(new Post(doc_id, temp_freq_tokens.get(token)));
                }

                docs_len.put(doc_id, count_tokens);
                total_len_docs += count_tokens;
            }
        }

        calBM25Ranking();

        return inverted_index;
    }

    @Override
    public Map<String, List<Post>> processIndexWithPositions() {
        Map<String, String> document_list;

        while ( !(document_list = corpusReader.readBlock()).isEmpty() ) {

            // counting the read documents to calculate idf
            N += document_list.size();

            for (String doc_id : document_list.keySet()) {
                Map<String, Integer> temp_freq_tokens = new HashMap<>();
                Map<String, List<Integer>> temp_list_pos = new HashMap<>();
                int count_tokens = 0;
                int pos = 0;

                for (String token : tokenizer.process_tokens(document_list.get(doc_id))) {

                    if (!token.isEmpty()) {
                        count_tokens++;
                        temp_freq_tokens.putIfAbsent(token, 0);
                        temp_freq_tokens.put( token, temp_freq_tokens.get(token) + 1 );

                        // extend the index to the position of the term in the doc
                        pos += 1;
                        temp_list_pos.putIfAbsent(token, new ArrayList<>());
                        temp_list_pos.get(token).add(pos);
                    }
                }

                for(String token: temp_freq_tokens.keySet()){
                    inverted_index.computeIfAbsent(token, k-> new ArrayList<>());
                    inverted_index.get(token).add(new Post(doc_id, temp_freq_tokens.get(token), temp_list_pos.get(token)));
                }

                docs_len.put(doc_id, count_tokens);
                total_len_docs += count_tokens;
            }
        }

        calBM25Ranking();

        return inverted_index;
    }

    private void calBM25Ranking(){

        double avdl = calAvgDocLen();

        for(String token : inverted_index.keySet()){
            for(Post post: inverted_index.get(token)){
                int temp_doc_len = docs_len.containsKey(post.getDocument_id()) ? docs_len.get(post.getDocument_id()) : post.getLength();
                post.BM25(k, b, avdl, temp_doc_len , N, inverted_index.get(token).size());
                countingTotalWeight(post);
            }
        }

    }

    private double calAvgDocLen(){
        return (double) total_len_docs / N;
    }

    public Map<String, List<Post>> processIndexWithMerge(String last_file_name, int memory_mb_max) throws IOException {
        clearTempFilesDirectory("temp_files");

        Map<String, String> document_list;
        int blocks_read = 0;

        double initial_memory_used = calculateMemory();

        while ( !(document_list = corpusReader.readBlock()).isEmpty() ) {

            // counting the read documents to calculate idf
            N += document_list.size();

            for (String doc_id : document_list.keySet()) {
                Map<String, Integer> temp_freq_tokens = new HashMap<>();
                Map<String, List<Integer>> temp_list_pos = new HashMap<>();
                int count_tokens = 0;
                int pos = 0;

                for (String token : tokenizer.process_tokens(document_list.get(doc_id))) {

                    if (!token.isEmpty()) {
                        count_tokens++;
                        temp_freq_tokens.putIfAbsent(token, 0);
                        temp_freq_tokens.put( token, temp_freq_tokens.get(token) + 1 );

                        // extend the index to the position of the term in the doc
                        pos += 1;
                        temp_list_pos.putIfAbsent(token, new ArrayList<>());
                        temp_list_pos.get(token).add(pos);
                    }
                }

                for(String token: temp_freq_tokens.keySet()){
                    inverted_index.computeIfAbsent(token, k-> new ArrayList<>());
                    inverted_index.get(token).add(new Post(doc_id, temp_freq_tokens.get(token), temp_list_pos.get(token)));
                }

                docs_len.put(doc_id, count_tokens);
                total_len_docs += count_tokens;
            }
            if ((calculateMemory() - initial_memory_used) > (memory_mb_max - 15)){
                blocks_read += 1;
                createTempFile(blocks_read, "");
                docs_len.clear();
                inverted_index.clear();
                System.gc();
                initial_memory_used = calculateMemory();
            }
        }
        blocks_read += 1;
        if (blocks_read == 1)
            calBM25Ranking();
        createTempFile(blocks_read, blocks_read == 1 ? last_file_name : "");
        docs_len.clear();
        inverted_index.clear();
        System.gc();

        return inverted_index;
    }

    @Override
    public void mergeFiles(String last_file_name, int memory_mb_max, int merges_at_the_time) throws IOException {

        File f = new File("temp_files");

        // This filter will only include conditional files
        FilenameFilter filter = (f1, name) -> (name.startsWith("temp_iindex_") || name.startsWith(last_file_name)) && name.endsWith(".txt");

        String[] actual_layer = f.list(filter);

        if (actual_layer.length == 1){
            return;
        }

        if (merges_at_the_time < 2) {
            throw new IllegalArgumentException("Cannot merge " + merges_at_the_time + " files at the time. Min = 2");
        }
        //if (merges_at_the_time > actual_layer.length) {
        //    throw new IllegalArgumentException("Cannot merge " + merges_at_the_time + " files at the time, with only " + actual_layer.length + " originals.");
        //}

        int actual_file = actual_layer.length;

        double initial_memory_used = calculateMemory();

        File[] merging_files = new File[merges_at_the_time];
        Scanner[] scanners = new Scanner[merges_at_the_time];
        String[] datum = new String[merges_at_the_time];
        String[] last_word = new String[merges_at_the_time];
        Arrays.fill(last_word, "");

        while (true) {
            actual_layer = f.list(filter);
            Arrays.sort(actual_layer);
            System.out.println("Lista: " + Arrays.toString(actual_layer));

            if (actual_layer.length == 1){
                System.out.println("Merging Done");
                break;
            }

            boolean last_layer = merges_at_the_time >= actual_layer.length;
            int merges_this_loop = Math.min(merges_at_the_time, actual_layer.length);
            System.out.println("Merges this Loop: " + merges_this_loop + "; LastLayer= " + last_layer);

            for (int i = 0; i < merges_this_loop; i++) {
                System.out.println("Adding: " + "temp_files/" + actual_layer[i]);
                merging_files[i] = new File("temp_files/" + actual_layer[i]);
                scanners[i] = new Scanner(merging_files[i]);
            }

            Map<String, List<Post>> inverted_index = new TreeMap<>();

            // initial retrieve (implies all files are not empty)
            for (int i = 0; i < merges_this_loop; i++) {
                if (last_word[i].isEmpty() && scanners[i].hasNextLine()) {

                    datum[i] = scanners[i].nextLine();
                    last_word[i] = datum[i].split(";")[0];
                }
            }

            boolean done = false;
            FileWriter myWriter = new FileWriter("temp_files/" + (last_layer ? last_file_name : ("temp_iindex_" + String.format("%02d", ++actual_file))) + ".txt");

            // merge process
            while (!done) {

                int actual_small_index = getStringIndex(last_word);

                String[] cols = datum[actual_small_index].split(";");

                List<Post> docs = new ArrayList<>();

                for (int i = 1; i < cols.length; i++) {
                    String[] attr = cols[i].split(":");

                    String[] pos = attr[2].split(",");
                    List<Integer> positions = new ArrayList<>();
                    for (String po : pos) {
                        positions.add(Integer.parseInt(po));
                    }
                    docs.add(new Post(attr[0], positions, Integer.parseInt(attr[1].split("-")[0]), Integer.parseInt(attr[1].split("-")[1])));

                }

                if (inverted_index.containsKey(cols[0])) {
                    inverted_index.get(cols[0]).addAll(docs);
                } else {
                    inverted_index.put(cols[0], docs);
                }

                if (scanners[actual_small_index].hasNextLine()) {
                    datum[actual_small_index] = scanners[actual_small_index].nextLine();
                    last_word[actual_small_index] = datum[actual_small_index].split(";")[0];
                } else {
                    last_word[actual_small_index] = "";
                }
                                                                        // 15 is a Off-set in MB, cause code inside usually adds about 15MB of more info
                if ((calculateMemory() - initial_memory_used) > (memory_mb_max - 15) || docsDoNotHaveNextLine(scanners, merges_this_loop)) {
                    if (docsDoNotHaveNextLine(scanners, merges_this_loop)) {
                        // while there are non empty last_words, aka clear all streams that still have a last term
                        while (Arrays.stream(last_word).filter(item -> !item.isEmpty()).min(String::compareTo).isPresent()) {

                            actual_small_index = getStringIndex(last_word);

                            cols = datum[actual_small_index].split(";");

                            docs = new ArrayList<>();

                            for (int i = 1; i < cols.length; i++) {
                                String[] attr = cols[i].split(":");

                                String[] pos = attr[2].split(",");
                                List<Integer> positions = new ArrayList<>();
                                for (String po : pos) {
                                    positions.add(Integer.parseInt(po));
                                }

                                docs.add(new Post(attr[0], positions, Integer.parseInt(attr[1].split("-")[0]), Integer.parseInt(attr[1].split("-")[1])));
                            }

                            if (inverted_index.containsKey(cols[0])) {
                                inverted_index.get(cols[0]).addAll(docs);
                            } else {
                                inverted_index.put(cols[0], docs);
                            }
                            last_word[actual_small_index] = "";
                        }

                        done = true;
                    }

                    double avdl = calAvgDocLen();

                    for (String token : inverted_index.keySet()) {

                        myWriter.write(token + (last_layer ? ":" + Math.log10((double) N / inverted_index.get(token).size()) : "") + ";");
                        for (Post post : inverted_index.get(token)) {
                            if (last_layer){
                                post.BM25(k, b, avdl, post.getLength(), N, inverted_index.get(token).size());
                            }

                            myWriter.write(post.getDocument_id() +
                                    (last_layer ? ":" + post.getWeight() : ":" + post.getLength() + "-" + post.getFreqToken())
                                    + ":" + post.getTextPositions() + ";");
                        }

                        myWriter.write("\n");
                    }

                    inverted_index.clear();
                    System.gc();
                    initial_memory_used = calculateMemory();
                }
            }

            myWriter.close();

            for (int i = 0; i < merges_this_loop; i++) {
                scanners[i].close();
                if (merging_files[i].delete()) {
                    System.out.println("Deleted the file: " + merging_files[i].getName());
                } else {
                    System.err.println("Failed to delete the file.");
                }
            }

        }
    }

    protected void createTempFile(int file_number, String final_file_name) {

        if (inverted_index.isEmpty()){
            return;
        }

        try{
            String directory = "temp_files";

            Files.createDirectories(Paths.get(directory));

            FileWriter myWriter;
            if (final_file_name.isEmpty())
                myWriter = new FileWriter("temp_files/temp_iindex_" + String.format("%02d", file_number) + ".txt");
            else
                myWriter = new FileWriter("temp_files/" + final_file_name + ".txt");

            for(String token : inverted_index.keySet()){
                /*
                Calculate IDF = log10(N/df)
                Df = Document Frequency = size of list associated the token
                */
//                myWriter.write(token + ":" + Math.log10( (double) N/inverted_index.get(token).size()) + ";");
                myWriter.write(token + (!final_file_name.isEmpty() ? ":" + Math.log10((double) N / inverted_index.get(token).size()) : "") + ";");
                for(Post post: inverted_index.get(token)){
                    myWriter.write(post.getDocument_id() + (!final_file_name.isEmpty() ? ":" + post.getWeight() : ":" + docs_len.get(post.getDocument_id()) + "-" + post.getFreqToken()) + ":" + post.getTextPositions() + ";");
                    //myWriter.write(post.getDocument_id() + ":" + docs_len.get(post.getDocument_id()) + "-" + post.getFreqToken() + ":" + post.getTextPositions() + ";");
                }

                myWriter.write("\n");
            }

            myWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
