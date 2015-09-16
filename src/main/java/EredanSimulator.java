public class EredanSimulator {
    public static final int SWORD = 0;
    public static final int RED = 1;
    public static final int BLUE = 2;
    public static final int YELLOW = 3;

    public static int[][] dicePossibilities = new int[84][6];

    public static void main(String[] args) {
        initDice();
        loadHeroes();
    }

    public static void initDice() {
        // Start at one before initial minimum value
        int[] dice = new int[] { SWORD - 1, SWORD, SWORD, SWORD, SWORD, SWORD };
        for (int x = 0; x < dicePossibilities.length; x++) {
            int pos = 0;

            // Find first index that can be incremented
            while (pos < dice.length && dice[pos] == YELLOW) {
                pos++;
            }

            // If we didn't find one, then all dice are max value
            if (pos == dice.length) {
                throw new RuntimeException("Tried to generate too many dice combinations");
            }

            // Increment the value at our found position and set all values before it to the same number
            dice[pos]++;
            for (int i = 0; i < pos; i++) {
                dice[i] = dice[pos];
            }

            dicePossibilities[x] = dice.clone();
        }
    }

    public static void loadHeroes() {

    }
}
