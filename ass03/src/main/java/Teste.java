import java.util.*;

public class Teste {
    public static void main(String[] args) {
        Map<String, Integer> order = new LinkedHashMap<>();

        order.put("ola", 1);
        order.put("adeus", 2);
        order.put("boa tader", 3);
        order.put("madrutgada", 1);

        for(String w : order.keySet())
            System.out.println(w);

        List<Integer> ola = new ArrayList<>();
        ola.add(2);
        ola.add(3);
        ola.add(1);

        ola.forEach(s -> System.out.print(s + ","));
    }
}
