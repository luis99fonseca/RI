import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public abstract class  Indexer {

    public final CorpusReader corpusReader;
    public final Tokenizer tokenizer;
    public final Map<String, Double> total_weights = new HashMap<>();

    // Number of collections
    protected int N = 0;

    public  TreeMap<String, List<Post>> inverted_index = new TreeMap<>();


    public Indexer(CorpusReader corpusReader, Tokenizer tokenizer) {
        this.corpusReader = corpusReader;
        this.tokenizer = tokenizer;
    }


    /*
   Given the documents and their tokens
   create inverted index as follows:
       token -> [ document1_id, document2_id, ... ]
    */
    public abstract Map<String, List<Post>> processIndexWithoutPositions();
    public abstract Map<String, List<Post>> processIndexWithPositions();
    public abstract Map<String, List<Post>> processIndexWithMerge(String last_file_name, int memory_mb_max) throws IOException;
    protected void clearTempFilesDirectory(String directory) throws IOException {

        Files.createDirectories(Paths.get(directory));

        File my_dir = new File(directory);

        FilenameFilter filter = (f1, name) -> name.endsWith(".txt");

        String[] files_to_delete = my_dir.list(filter);

        assert files_to_delete != null;

        // clean files in directory
        for(String name_file : files_to_delete){

            String path = directory + "/" + name_file;

            if (!new File(path).delete())
                System.out.println("Error cleaning the directory " + directory);

        }
        System.out.println("Directory " + directory + " clean");
    }

    public abstract void mergeFiles(String last_file_name, int memory_mb_max) throws IOException;


    protected int getStringIndex(String[] arr) {
        String smallest = Arrays.stream(arr).filter(item -> !item.isEmpty()).min(String::compareTo).orElse(null);
        for (int l = 0; l < arr.length; l++) {
            if (arr[l].equals(smallest)) {
                return l;
            }
        }
        return 0;
    }

    protected boolean docsDoNotHaveNextLine(Scanner[] scanners, int merges_this_loop) {
        for (int i = 0; i < merges_this_loop; i++){
            if (scanners[i].hasNextLine()) {
                return false;
            }
        }
        return true;
    }

    public void countingTotalWeight(Post post){
        String doc_id = post.getDocument_id();

        total_weights.putIfAbsent(doc_id, 0.0);
        total_weights.put(doc_id, total_weights.get(doc_id) + Math.pow(post.getWeight(), 2));
    }

    public void normalizeWt(){
        for(String token : inverted_index.keySet())
            for(Post post: inverted_index.get(token))
                post.setWeight( post.getWeight() / Math.sqrt(total_weights.get(post.getDocument_id())) );
    }

    public void writeInFileWithoutPositions(String file_name){

        try{
            FileWriter myWriter = new FileWriter(file_name);

            for(String token : inverted_index.keySet()){
                /*
                Calculate IDF = log10(N/df)
                Df = Document Frequency = size of list associated the token
                */
                myWriter.write(token + ":" + Math.log10( (double) N/inverted_index.get(token).size()) + ";");

                for(Post post: inverted_index.get(token)){
                    myWriter.write(post.getDocument_id() + ":" + post.getWeight()+";");
                }

                myWriter.write("\n");
            }

            myWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeInFileWithPositions(String file_name){

        try{
            FileWriter myWriter = new FileWriter(file_name);

            for(String token : inverted_index.keySet()){
                /*
                Calculate IDF = log10(N/df)
                Df = Document Frequency = size of list associated the token
                */
                myWriter.write(token + ":" + Math.log10( (double) N/inverted_index.get(token).size()) + ";");

                for(Post post: inverted_index.get(token)){
                    myWriter.write(post.getDocument_id() + ":" + post.getWeight()+":" + post.getTextPositions() + ";");
                }

                myWriter.write("\n");
            }

            myWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract void createTempFile(int file_number, String final_file_name);

    public void splitMergedFile(String file_name, int memory_mb_max){

        try {
            // TODO: ver se ta bem :)
            String directory = "index_split";
            clearTempFilesDirectory(directory);

//
//            Files.createDirectories(Paths.get(directory));
//
//            File my_dir = new File(directory);
//
//            FilenameFilter filter = (f1, name) -> name.endsWith(".txt");
//
//            String[] files_to_delete = my_dir.list(filter);
//
//            assert files_to_delete != null;
//
//            // clean files in directory
//            for(String name_file : files_to_delete){
//
//                String path = directory + "/" + name_file;
//
//                if (!new File(path).delete())
//                    System.out.println("Error cleaning the directory " + directory);
//
//            }
//            System.out.println("Directory " + directory + " clean");

            File myFile = new File("temp_files/" +file_name + ".txt");
            Scanner myReader = new Scanner(myFile);
            boolean with_positions = false;

            double initial_memory_used = calculateMemory();

            while (myReader.hasNextLine()) {

                String data = myReader.nextLine();
                String[] cols = data.split(";");
                List<Post> docs = new ArrayList<>();

                for (int i = 1; i < cols.length; i++) {
                    String[] attr = cols[i].split(":");

                    if (attr.length == 2)

                        // without positions
                        docs.add(new Post(attr[0], Double.parseDouble(attr[1])));

                    else {

                        // with positions
                        with_positions = true;

                        String[] pos = attr[2].split(",");
                        List<Integer> positions = new ArrayList<>();

                        for (String p : pos) {
                            positions.add(Integer.parseInt(p));
                        }

                        docs.add(new Post(attr[0], Double.parseDouble(attr[1]), positions));
                    }
                }

                String[] token_info = cols[0].split(":");

                inverted_index.put(token_info[0], docs);

                if ((calculateMemory() - initial_memory_used) >= memory_mb_max) {

                    writeInDisk(with_positions, directory);

                    initial_memory_used = calculateMemory();
                }
            }

            if(!inverted_index.isEmpty())
                writeInDisk(with_positions, directory);

        } catch (FileNotFoundException e) {
            System.err.println("Error: Fail read indexer file!");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void writeInDisk(boolean with_positions, String directory){

        // create name file to write inverted index
        String name_file = directory + "/" + inverted_index.firstKey() +
                "-" + inverted_index.lastKey() + ".txt";

        // check if results file is calculated with proximity match rank boost or not
        if(with_positions)
            writeInFileWithPositions(name_file);
        else
            writeInFileWithoutPositions(name_file);

        inverted_index = new TreeMap<>();
        System.gc(); // free memory
    }

    protected double calculateMemory() {

        Runtime runtime = Runtime.getRuntime();
        //long memoryMax = runtime.maxMemory();
        long memoryUsed = runtime.totalMemory() - runtime.freeMemory();
        //double memoryUserPercentage = ((double) (memoryUsed * 100)) / memoryMax;

        //memory used in mb
        return memoryUsed * Math.pow(10, -6);


    }
}
