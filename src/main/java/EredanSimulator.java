import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.Ability;
import dto.Effect;
import dto.Hero;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class EredanSimulator {
    public static Logger log = Logger.getLogger("foo");
    public static final int TIE = 0;
    public static final int P1_WIN = 1;
    public static final int P2_WIN = 2;

    public static final int SWORD = 0;
    public static final int RED = 1;
    public static final int BLUE = 2;
    public static final int YELLOW = 3;

    public static int[][] dicePossibilities = new int[84][6];
    public static Boolean[][][] keepPossibilities = new Boolean[84][][];

    public static List<Hero> heroes;

    public static Ability basicAttack = new Ability();
    static {
        Effect e = new Effect("swordAttack", 1);
        List<Effect> effects = new ArrayList<>(1);
        effects.add(e);

        basicAttack.effects = effects;
        basicAttack.setCost("S");
    }

    public static void main(String[] args) {
        initDice();
        loadHeroes();

        // Generate stats
        for (int i = 0; i < heroes.size(); i++) {
            System.out.print("\t" + heroes.get(i).name);
        }
        System.out.println();

        for (int i = 0; i < heroes.size(); i++) {
            System.out.print(heroes.get(i).name);
            for (int j = 0; j < heroes.size(); j++) {
                System.out.print("\t" + sumResults(simulate(heroes.get(i), heroes.get(j))));
            }
            System.out.println();
        }
    }

    public static double sumResults(int[][] results) {
        int p1 = 0;
        for (int i = 0; i < results.length; i++) {
            for (int j = 0; j < results[i].length; j++) {
                if (results[i][j] == P1_WIN) {
                    p1++;
                }
            }
        }

        return (double)p1 / 84 / 84 * 100;
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

            keepPossibilities[x] = keepOptions.toArray(new Boolean[0][0]);
        }
    }

    public static void loadHeroes() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            heroes = objectMapper.readValue(new File("heroes.json"), new TypeReference<List<Hero>>() { });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int[][] simulate(Hero p1, Hero p2) {
        int[][] results = new int[84][84];
        for (int i = 0; i < dicePossibilities.length; i++) {
            for (int j = 0; j < dicePossibilities.length; j++) {
                results[i][j] = simulate(p1, p2, i, j);
            }
        }

        return results;
    }

    public static int simulate(Hero p1, Hero p2, int p1idx, int p2idx) {
        CharacterStatus p1status = new CharacterStatus(p1, dicePossibilities[p1idx]);
        p1status.isAttacker = true;
        CharacterStatus p2status = new CharacterStatus(p2, dicePossibilities[p2idx]);

        log.trace(Arrays.toString(p1status.dice));
        log.trace(Arrays.toString(p2status.dice));

        // Abilities
        for (int round = 0; round < 3; round++) {
            BattleActionResolver.execute(p1.abilities.get(round), p1status, p2status);
            BattleActionResolver.execute(p2.abilities.get(round), p2status, p1status);
        }

        // Swords
        BattleActionResolver.execute(basicAttack, p1status, p2status);
        BattleActionResolver.execute(basicAttack, p2status, p1status);

        if (p1status.damage < p2status.damage) {
            log.trace("P1 WIN");
            return P1_WIN;
        } else if (p1status.damage > p2status.damage) {
            log.trace("P2 WIN");
            return P2_WIN;
        } else {
            log.trace("TIE");
            return TIE;
        }
    }
}
