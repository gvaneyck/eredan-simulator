package simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Dice {
    public static Random rand = new Random();

    public static int[][] possibilities = new int[84][6];
    public static int[][] counts = new int[84][4];
    public static Boolean[][][] keeps = new Boolean[84][][];
    public static Map<Integer, Integer> toIndex = new HashMap<>();

    public static final int SWORD = 0;
    public static final int RED = 1;
    public static final int BLUE = 2;
    public static final int YELLOW = 3;

    static {
        initDice();
    }

    private static void initDice() {
        // Start at one before initial minimum value
        int[] dice = new int[] { SWORD - 1, SWORD, SWORD, SWORD, SWORD, SWORD };
        for (int x = 0; x < possibilities.length; x++) {
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

            possibilities[x] = dice.clone();

            int[] diceCount = new int[4];
            for (int i = 0; i < dice.length; i++) {
                diceCount[dice[i]]++;
            }
            counts[x] = diceCount;

            toIndex.put(dice[0] + (dice[1] << 2) + (dice[2] << 4) + (dice[3] << 6) + (dice[4] << 8) + (dice[5] << 10), x);

            List<Boolean[]> keepOptions = new ArrayList<>();
            for (int i = 0; i < 64; i++) {
                keepOptions.add(new Boolean[] {
                        ((i & 1) != 0),
                        ((i & 2) != 0),
                        ((i & 4) != 0),
                        ((i & 8) != 0),
                        ((i & 16) != 0),
                        ((i & 32) != 0)
                });
            }

            Iterator<Boolean[]> iter = keepOptions.iterator();
            while (iter.hasNext()) {
                Boolean[] vals = iter.next();
                boolean keep = true;
                for (int i = 5; keep && i >= 0; i--) {
                    if (vals[i]) {
                        for (int j = i - 1; j >= 0; j--) {
                            if (dice[i] != dice[j]) {
                                break;
                            } else if (!vals[j]) {
                                keep = false;
                                break;
                            }
                        }
                    }
                }

                if (!keep) {
                    iter.remove();
                }
            }

            keeps[x] = keepOptions.toArray(new Boolean[keepOptions.size()][6]);
        }
    }

    public static int roll(int idx, int keepIdx) {
        List<Integer> newDice = new ArrayList<>(6);
        for (int i = 0; i < 6; i++) {
            if (keeps[idx][keepIdx][i]) {
                newDice.add(possibilities[idx][i]);
            } else {
                int val = rand.nextInt(6);
                newDice.add(val == 5 ? YELLOW : val / 2);
            }
        }
        Collections.sort(newDice);
        Collections.reverse(newDice);
        return toIndex.get(newDice.get(0) + (newDice.get(1) << 2) + (newDice.get(2) << 4) + (newDice.get(3) << 6) + (newDice.get(4) << 8) + (newDice.get(5) << 10));
    }
}
