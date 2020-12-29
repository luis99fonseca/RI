import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Teste {
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
        Files.createDirectories(Paths.get("temp_files"));
        FileWriter myWriter = new FileWriter("temp_files/somename01.txt");

        myWriter.write("ola");
        myWriter.write("ola2");

        myWriter.write("ola3");

        myWriter.write("ola4");

        myWriter.close();

    }
}
