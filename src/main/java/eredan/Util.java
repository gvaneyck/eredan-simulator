package eredan;

import java.util.Random;

public class Util {
    private static Random rand = new Random();

    public static void shuffleArray(Object[] array) {
        int index;
        Object temp;
        for (int i = array.length - 1; i > 0; i--) {
            index = rand.nextInt(i + 1);
            temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }
}
