import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;

public class FileReader {
    private String fileName;
    public List<Weapen> fileList;

    public FileReader(String fileName, List<Weapen> fileList) {
        this.fileName = fileName;
        this.fileList = fileList;
    }

    public List<Weapen> toList(){
        try {
            fileList= Files.lines(new File(fileName).toPath())
                    .skip(1)
                    .map(n -> n.split(";"))
                    .map(n ->new Weapen(n[0],CombatType.valueOf(n[1]),DamageType.valueOf(n[2]),Integer.parseInt(n[3]),Integer.parseInt(n[4]),Integer.parseInt(n[5]),Integer.parseInt(n[6])))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileList;
    }
    public List<Weapen> sortedList() {
        try {
            fileList = Files.lines(new File(fileName).toPath())
                    .skip(1)
                    .map(n -> n.split(";"))
                    .map(n -> new Weapen(n[0], CombatType.valueOf(n[1]), DamageType.valueOf(n[2]), Integer.parseInt(n[3]), Integer.parseInt(n[4]), Integer.parseInt(n[5]), Integer.parseInt(n[6])))
                    .toList()
                    .stream().sorted(Comparator.comparing(Weapen::getDamage)
                            .reversed().thenComparing(Weapen::getCombatType).thenComparing(Weapen::getDamageType).thenComparing(Weapen::getName))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileList;
    }

}
