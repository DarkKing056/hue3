import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileReaderTest {

    @Test
    void toList() {
        List<Weapen> testList=new ArrayList<>();

        FileReader fileReader=new FileReader("weapons.csv",testList);
        testList=fileReader.toList();

        //Test for Normal list
        assertEquals(testList.get(1).getDamage(),1);
    }
    @Test
    void sortedList(){
        List<Weapen> testList=new ArrayList<>();

        FileReader fileReader=new FileReader("weapons.csv",testList);

        testList=fileReader.sortedList();


        //Test if damage sorted
        assertEquals(testList.get(0).getDamage(),17);
        //Test sort by combatType
        assertEquals(testList.get(15).getCombatType(),CombatType.NONE);
        //Test for damageType
        assertEquals(testList.get(7).getDamageType(),DamageType.SLASHING);
        //Test for name
        assertEquals(testList.get(10).getName(),"Kundane");
    }
}