import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

public class TesteLuis3 {
    public static void main(String[] args) throws IOException {

        File f = new File("temp_files");
        System.out.println(Arrays.toString(f.list()));
        File myObj = new File("temp_files/filename.txt");
        if (myObj.createNewFile()) {
            System.out.println("File created: " + myObj.getName());
        } else {
            System.out.println("File already exists.");
        }
        File r = new File("temp_files");
        System.out.println(">> "+ Arrays.toString(r.list()));
        System.out.println("<<" +Arrays.toString(f.list()));
//        File myObj2 = new File("temp_files/filename.txt");
//        if (myObj2.delete()) {
//            System.out.println("Deleted the file: " + myObj2.getName());
//        } else {
//            System.out.println("Failed to delete the file.");
//        }
//        File r3 = new File("temp_files");
//        System.out.println(Arrays.toString(r3.list()));
        System.exit(-1);

        int actual_file = 0;
        int actual_max_file = 3;    // original / initial files
        int next_max = actual_max_file;
        boolean last_layer = false;

        int pair_count = 0;
        File myFile;

        int total_lines_this_loop = 0;
        final int max_lines_per_loop = 250;

        int merges_at_the_time = 2;

        if(merges_at_the_time > actual_max_file) {
            throw new IllegalArgumentException("Cannot merge " + merges_at_the_time + " files at the time, with only " + actual_max_file + " originals.");
        }

        Scanner[] scanners = new Scanner[merges_at_the_time];
        String[] datum = new String[merges_at_the_time];
        String[] last_word = new String[merges_at_the_time];
        Arrays.fill(last_word, "");

        System.out.println(Arrays.toString(last_word));

        while (true){
            actual_file += 1;
            System.out.println("actual_file " + actual_file
                    + "; actual_max_file " + actual_max_file
                    + "; next_max " + next_max
                    + "; pair_count " + pair_count
                    + "; impar: " + actual_max_file % 2);

            // if there are merges to be done in the actual layer
            if (actual_file <= actual_max_file){
                System.out.println("IF file: " + actual_file);
                myFile = new File("temp_files/temp_iindex_" + actual_file + ".txt");
                scanners[pair_count] = new Scanner(myFile);

                pair_count += 1;
            } else {
                break;
            }

            // add to next layer
            if (pair_count > 0 && pair_count % scanners.length == 0){
                System.out.println("IF pairs");
                System.out.println("IF PAIRS; actual_file " + actual_file
                        + "; actual_max_file " + actual_max_file
                        + "; next_max " + next_max
                        + "; pair_count " + pair_count
                        + "; impar: " + actual_max_file % 2
                        + "; " );
                pair_count = 0; // its counted despite sometimes IDE saying otherwise
                next_max += 1;

                Map<String, List<Post>> inverted_index = new TreeMap<>();

                boolean done = false;
                FileWriter myWriter = new FileWriter("temp_files/temp_iindex_" + (next_max) + ".txt");

                // initial retrieve (implies all files are not empty)
                for (int i = 0; i < merges_at_the_time; i++){
                    if (last_word[i].isEmpty() && scanners[i].hasNextLine()){

                        datum[i] = scanners[i].nextLine();
                        last_word[i] = datum[i].split(";")[0];
                        total_lines_this_loop += 1;
                    }
                }

                while (!done){


                    int actual_small_index = getStringIndex(last_word, "min");

                    String[] cols = datum[actual_small_index].split(";");

                    List<Post> docs = new ArrayList<>();

                    for(int i = 1; i < cols.length; i++){
                        String[] attr = cols[i].split(":");

                        String[] pos = attr[2].split(",");
                        List <Integer> positions = new ArrayList<>();
                        for (String po : pos) {
                            positions.add(Integer.parseInt(po));
                        }
                        docs.add(new Post(attr[0], Double.parseDouble(attr[1]), positions));
                    }

                    if (inverted_index.containsKey(cols[0])){
                        inverted_index.get(cols[0]).addAll(docs);
                    } else {
                        inverted_index.put(cols[0], docs);
                    }

                    if (scanners[actual_small_index].hasNextLine()){
                        datum[actual_small_index] = scanners[actual_small_index].nextLine();
                        last_word[actual_small_index] = datum[actual_small_index].split(";")[0];
                        total_lines_this_loop += 1;
                    } else {
                        last_word[actual_small_index] = "";
                    }

                    if (total_lines_this_loop >= max_lines_per_loop || docsDoNotHaveNextLine(scanners)){
//                        System.out.println("WRITING: " + total_lines_this_loop  + ";" + Arrays.toString(last_word));

                        if (docsDoNotHaveNextLine(scanners)){

                            // while there are non empty last_words, aka clear all streams that still have a last term
                            while (Arrays.stream(last_word).filter(item-> !item.isEmpty()).min(String::compareTo).isPresent()){
//                                System.out.println("BOOl: " + Arrays.stream(last_word).filter(item-> !item.isEmpty()).min(String::compareTo).isPresent() + "; " + Arrays.toString(last_word));
                                actual_small_index = getStringIndex(last_word, "min");
//                                System.out.println("GRANDE: " + datum[actual_small_index]);

                                cols = datum[actual_small_index].split(";");

                                docs = new ArrayList<>();

                                for(int i = 1; i < cols.length; i++){
                                    String[] attr = cols[i].split(":");

                                    String[] pos = attr[2].split(",");
                                    List <Integer> positions = new ArrayList<>();
                                    for (String po : pos) {
                                        positions.add(Integer.parseInt(po));
                                    }
                                    docs.add(new Post(attr[0], Double.parseDouble(attr[1]), positions));
                                }

                                if (inverted_index.containsKey(cols[0])){
                                    inverted_index.get(cols[0]).addAll(docs);
                                } else {
                                    inverted_index.put(cols[0], docs);
                                }
                                last_word[actual_small_index] = "";
                            }

                            done = true;
                        }

                        for(String token : inverted_index.keySet()){

                            myWriter.write(token + (last_layer? ":" + Math.log10( (double) 37912/inverted_index.get(token).size()) : "") + ";");
                            for(Post post: inverted_index.get(token)){

                                myWriter.write(post.getDocument_id() + ":" + post.getWeight()+":" + post.getTextPositions() + ";");
                            }

                            myWriter.write("\n");
                        }

                        inverted_index.clear();
                        total_lines_this_loop = 0;


                    }

                }

                myWriter.close();

                for (int i = 0; i < merges_at_the_time; i++){
                    scanners[i].close();
                }
                if (last_layer){
                    System.out.println("VOU FECHAR");
                }
            }

            // next layer
            if (actual_file == actual_max_file){
                System.out.println("IF next layer-----------------" +  actual_file + "; " + next_max);
                if (next_max == actual_max_file + merges_at_the_time){
                    System.out.println("ULTIMO");
                    last_layer = true;
                }
                actual_max_file = next_max;
            }

        }

    }

    private static int getStringIndex(String[] arr, String order){
//        System.out.println("compare: " + Arrays.toString(arr));
        String smallest = Arrays.stream(arr).filter(item-> !item.isEmpty()).min(String::compareTo).orElse(null);
        for(int l = 0; l < arr.length; l++){
            if (arr[l].equals(smallest)){
                return l;
            }
        }
        return 0;
    }

    private static boolean docsDoNotHaveNextLine(Scanner[] scanners){
        for (Scanner s : scanners){
            if (s.hasNextLine()){
                return false;
            }
        }
        return true;
    }
}
