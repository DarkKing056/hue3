import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        List<Weapen> weapens=new ArrayList<>();

        FileReader fileReader=new FileReader("weapons.csv",weapens);

        fileReader.toList();
        for (int i = 0; i < weapens.size(); i++) {
            System.out.println(weapens.get(i));
        }

    }
}