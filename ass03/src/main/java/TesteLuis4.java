import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

public class TesteLuis4 {
    public static void main(String[] args) throws IOException {

        File f = new File("temp_files");

        // This filter will only include conditional files
        FilenameFilter filter = (f1, name) -> name.startsWith("temp_iindex_") && name.endsWith(".txt");

        String[] actual_layer = f.list(filter);

        int merges_at_the_time = 3;
        if (merges_at_the_time < 2) {
            throw new IllegalArgumentException("Cannot merge " + merges_at_the_time + " files at the time. Min = 2");
        }
        if (merges_at_the_time > actual_layer.length) {
            throw new IllegalArgumentException("Cannot merge " + merges_at_the_time + " files at the time, with only " + actual_layer.length + " originals.");
        }

        int actual_file = actual_layer.length;

        // TODO: fazer com Memoria
        int total_lines_this_loop = 0;
        final int max_lines_per_loop = 250;

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
                System.out.println("FINALLY DONE");
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
                    total_lines_this_loop += 1;
                }
            }

            boolean done = false;
            FileWriter myWriter = new FileWriter("temp_files/temp_iindex_" + String.format("%02d", ++actual_file) + ".txt");

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
                    docs.add(new Post(attr[0], Double.parseDouble(attr[1]), positions));
                }

                if (inverted_index.containsKey(cols[0])) {
                    inverted_index.get(cols[0]).addAll(docs);
                } else {
                    inverted_index.put(cols[0], docs);
                }

                if (scanners[actual_small_index].hasNextLine()) {
                    datum[actual_small_index] = scanners[actual_small_index].nextLine();
                    last_word[actual_small_index] = datum[actual_small_index].split(";")[0];
                    total_lines_this_loop += 1;
                } else {
                    last_word[actual_small_index] = "";
                }

                if (total_lines_this_loop >= max_lines_per_loop || docsDoNotHaveNextLine(scanners, merges_this_loop)) {
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
                                docs.add(new Post(attr[0], Double.parseDouble(attr[1]), positions));
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

                    for (String token : inverted_index.keySet()) {

                        myWriter.write(token + (last_layer ? ":" + Math.log10((double) 37912 / inverted_index.get(token).size()) : "") + ";");
                        for (Post post : inverted_index.get(token)) {

                            myWriter.write(post.getDocument_id() + ":" + post.getWeight() + ":" + post.getTextPositions() + ";");
                        }

                        myWriter.write("\n");
                    }

                    inverted_index.clear();
                    total_lines_this_loop = 0;

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

    private static int getStringIndex(String[] arr) {
//        System.out.println("compare: " + Arrays.toString(arr));
        String smallest = Arrays.stream(arr).filter(item -> !item.isEmpty()).min(String::compareTo).orElse(null);
        for (int l = 0; l < arr.length; l++) {
            if (arr[l].equals(smallest)) {
                return l;
            }
        }
        return 0;
    }

    private static boolean docsDoNotHaveNextLine(Scanner[] scanners, int merges_this_loop) {
        for (int i = 0; i < merges_this_loop; i++){
            if (scanners[i].hasNextLine()) {
                return false;
            }
        }
        return true;
    }
}
