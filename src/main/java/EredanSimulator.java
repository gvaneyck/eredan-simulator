import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.Ability;
import dto.Effect;
import dto.Hero;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EredanSimulator {
    public static final int TIE = 0;
    public static final int P1_WIN = 1;
    public static final int P2_WIN = 2;

    public static final int SWORD = 0;
    public static final int RED = 1;
    public static final int BLUE = 2;
    public static final int YELLOW = 3;

    public static final int[] SWORD_COST = new int[] { 1, 0, 0, 0 };

    public static int[][] dicePossibilities = new int[84][6];
    public static Boolean[][][] keepPossibilities = new Boolean[84][][];

    public static List<Hero> heroes;

    public static void main(String[] args) {
        initDice();
        loadHeroes();
        int[][] results = simulate(heroes.get(0), heroes.get(1));
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

            List<Boolean[]> keepOptions = new ArrayList<Boolean[]>();
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
                int[] p1dice = dicePossibilities[i];
                int[] p2dice = dicePossibilities[j];
                CharacterStatus p1status = new CharacterStatus(p1);
                CharacterStatus p2status = new CharacterStatus(p2);

                // Abilities
                for (int round = 0; round < 3; round++) {
                    Ability p1a = p1.abilities.get(round);
                    int triggers1 = triggers(p1dice, p1a.cost);
                    for (int x = 0; x < triggers1; x++) {
                        for (Effect e : p1a.effects) {
                            BattleActionResolver.execute(e.effect, e.amount, p1status, p2status);
                        }
                    }

                    Ability p2a = p2.abilities.get(round);
                    int triggers2 = triggers(p2dice, p2a.cost);
                    for (int x = 0; x < triggers2; x++) {
                        for (Effect e : p2a.effects) {
                            BattleActionResolver.execute(e.effect, e.amount, p2status, p1status);
                        }
                    }
                }

                // Swords
                BattleActionResolver.execute("hit", triggers(p1dice, SWORD_COST), p1status, p2status);
                BattleActionResolver.execute("hit", triggers(p2dice, SWORD_COST), p2status, p1status);

                if (p1status.damage < p2status.damage) {
                    results[i][j] = P1_WIN;
                } else if (p1status.damage > p2status.damage) {
                    results[i][j] = P2_WIN;
                } else {
                    results[i][j] = TIE;
                }
            }
        }

        return results;
    }

    public static int triggers(int[] dice, int[] cost) {
        int triggers = 6;
        int[] diceCounts = new int[4];
        for (int i = 0; i < dice.length; i++) {
            diceCounts[dice[i]]++;
        }

        for (int i = 0; i < diceCounts.length; i++) {
            if (cost[i] != 0) {
                triggers = Math.min(triggers, diceCounts[i] / cost[i]);
            }
        }

        return triggers;
    }
}
