import java.util.*;

public class Teste {
    public void printaca(){
        System.out.println("ola");
    }

    public static void main(String[] args) {
        //Testev2 x = new Testev2();
        //x.printaca();
        List<Post> list = new ArrayList<>();
        Post a = new Post("abab");
        list.add(a);

        Post t_p = new Post("abab");

        int i = list.indexOf(t_p);

        if( i != -1 )
            System.out.println("ola");
        else
            System.out.println("adeus");
    }
}
