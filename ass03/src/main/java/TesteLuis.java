import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TesteLuis {
    public static void main(String[] args) throws IOException {
//        Map<String, Integer> order = new LinkedHashMap<>();
//
//        order.put("ola", 1);
//        order.put("adeus", 2);
//        order.put("boa tader", 3);
//        order.put("madrutgada", 1);
//
//        for(String w : order.keySet())
//            System.out.println(w);
//
//        List<Integer> ola = new ArrayList<>();
//        ola.add(2);
//        ola.add(3);
//        ola.add(1);
//
//        ola.forEach(s -> System.out.print(s + ","));
//        Files.createDirectories(Paths.get("temp_files"));
//        FileWriter myWriter = new FileWriter("temp_files/somename01.txt");
//
//        myWriter.write("ola");
//        myWriter.write("ola2");
//
//        myWriter.write("ola3");
//
//        myWriter.write("ola4");
//
//        myWriter.close();

        int actual_file = 0;
        int actual_max_file = 8;    // original / initial files
        int next_max = actual_max_file;

        boolean done = false;

        int pair_count = 0;
        File myFile;
        Scanner [] scanners = new Scanner[2];

        while (!done){
            actual_file += 1;
            System.out.println("actual_file " + actual_file
                    + "; actual_max_file " + actual_max_file
                    + "; next_max " + next_max
                    + "; pair_count " + pair_count
                    + "; impar: " + actual_max_file % 2);

            // if there are merges to be done in the actual layer
            if (actual_file <= actual_max_file){
                System.out.println("IF file: " + actual_file);
                myFile = new File("temp_files/temp_iindex_" + actual_file);
                scanners[pair_count] = new Scanner(myFile);

                pair_count += 1;
            } else {
                break;
            }

            // add to next layer
            if (pair_count > 0 && pair_count % scanners.length == 0){
                System.out.println("IF pairs");
                pair_count = 0;
                next_max += 1;
                FileWriter myWriter = new FileWriter("temp_files/temp_iindex_" + (next_max));
//                myWriter.write( "> Scanner:" + 0 + "- file:" + (actual_file - 1) );
//                myWriter.write( "; > Scanner" + 1 + "- file:" + (actual_file - 0) + "\n");
                myWriter.write((actual_file - 1) + "&" + (actual_file - 0) + ";");
                myWriter.close();
                scanners[0].close();
                scanners[1].close();
            }

            // next layer
            if (actual_file == actual_max_file){
                System.out.println("IF next layer-----------------");
                actual_max_file = next_max;
            }

//            // if we reach last layer
//            if (actual_max_file == next_max){
//                System.out.println("IF done");
//                done = true;
//            }
//            if (actual_file > 18)
//                    done = true;
        }

    }
}
