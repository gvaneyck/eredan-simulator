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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class EredanSimulator {

    public static Random rand = new Random();
    public static Logger log = Logger.getLogger("foo");
    public static final int TIE = 0;
    public static final int P1_WIN = 1;
    public static final int P2_WIN = 2;

    public static final int SWORD = 0;
    public static final int RED = 1;
    public static final int BLUE = 2;
    public static final int YELLOW = 3;

    public static int[][] dicePossibilities = new int[84][6];
    public static int[][] diceCounts = new int[84][4];
    public static Boolean[][][] keepPossibilities = new Boolean[84][][];
    public static Map<Integer, Integer> diceToIndex = new HashMap<>();

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
        for (int i = 0; i < heroes.size(); i++) {
            for (int j = 0; j < heroes.size(); j++) {
                mcts(i, j);
                log.debug("--------------------");
            }
        }

//        // Generate stats
//        for (int i = 0; i < heroes.size(); i++) {
//            System.out.print("\t" + heroes.get(i).name);
//        }
//        System.out.println();
//
//        for (int i = 0; i < heroes.size(); i++) {
//            System.out.print(heroes.get(i).name);
//            for (int j = 0; j < heroes.size(); j++) {
//                System.out.print("\t" + sumResults(simulate(heroes.get(i), heroes.get(j))));
//            }
//            System.out.println();
//        }
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

            int[] diceCount = new int[4];
            for (int i = 0; i < dice.length; i++) {
                diceCount[dice[i]]++;
            }
            diceCounts[x] = diceCount;

            diceToIndex.put(dice[0] + (dice[1] << 2) + (dice[2] << 4) + (dice[3] << 6) + (dice[4] << 8) + (dice[5] << 10), x);

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

            keepPossibilities[x] = keepOptions.toArray(new Boolean[keepOptions.size()][6]);
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
        CharacterStatus p1status = new CharacterStatus(p1, diceCounts[p1idx]);
        p1status.isAttacker = true;
        CharacterStatus p2status = new CharacterStatus(p2, diceCounts[p2idx]);

        log.trace(Arrays.toString(dicePossibilities[p1idx]));
        log.trace(Arrays.toString(dicePossibilities[p2idx]));

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

    public static int rollDice(int idx, int keepIdx) {
        List<Integer> newDice = new ArrayList<>(6);
        for (int i = 0; i < 6; i++) {
            if (keepPossibilities[idx][keepIdx][i]) {
                newDice.add(dicePossibilities[idx][i]);
            } else {
                int val = rand.nextInt(6);
                newDice.add(val == 5 ? YELLOW : val / 2);
            }
        }
        Collections.sort(newDice);
        Collections.reverse(newDice);
        return diceToIndex.get(newDice.get(0) + (newDice.get(1) << 2) + (newDice.get(2) << 4) + (newDice.get(3) << 6) + (newDice.get(4) << 8) + (newDice.get(5) << 10));
    }

    public static void mcts(int h1, int h2) {
        int[][] battleResults = simulate(heroes.get(h1), heroes.get(h2));
        for (int i = 0; i < 1000000; i++) {
            runSim(h1, h2, battleResults);
        }

        double p1win = 0, p2win = 0;
        int p1sets = 0, p2sets = 0;
        for (int key : infoSets.keySet()) {
            InfoSet set = infoSets.get(key);
            if (set.me == h1 && set.them == h2 && set.attacker && set.round == 0) {
                p1win += (double)set.childStats[set.bestChild()].wins / set.childStats[set.bestChild()].visits;
                p1sets++;
//                int bestOption = set.bestChild();
//                Boolean[] keep = keepPossibilities[set.dice][bestOption];
//                String keepString = String.format("[%d, %d, %d, %d, %d, %d]", (keep[0] ? 1 : 0), (keep[1] ? 1 : 0), (keep[2] ? 1 : 0), (keep[3] ? 1 : 0), (keep[4] ? 1 : 0), (keep[5] ? 1 : 0));
//                log.debug(Arrays.toString(dicePossibilities[set.dice]) + " " + keepString + " " + set.childStats[bestOption].score * 100);
            } else if (set.me == h2 && set.them == h1 && !set.attacker && set.round == 0) {
                p2win += (double)set.childStats[set.bestChild()].wins / set.childStats[set.bestChild()].visits;
                p2sets++;
            }
        }

        log.debug(heroes.get(h1).name + " VS " + heroes.get(h2).name);
        log.debug((p1win / p1sets * 100) + " VS " + (p2win / p2sets * 100));
    }

    public static Map<Integer, InfoSet> infoSets = new HashMap<>();

    public static void runSim(int h1, int h2, int[][] battleResults) {
        // Transient state used to look up real stat tracking infosets
        InfoSet p1state = new InfoSet(0, true, h1, h2, rand.nextInt(84), 0);
        InfoSet p2state = new InfoSet(0, false, h2, h1, rand.nextInt(84), 0);

        InfoSet p1r0 = getInfoSet(p1state);
        int p1r0best = p1r0.bestChild();
        p1r0.visits++;
        p1r0.childStats[p1r0best].visit(p1r0.visits);
        p1state.dice = rollDice(p1state.dice, p1r0best);
        p1state.round++;

        InfoSet p1r1 = getInfoSet(p1state);
        int p1r1best = p1r1.bestChild();
        p1r1.visits++;
        p1r1.childStats[p1r1best].visit(p1r1.visits);
        p1state.dice = rollDice(p1state.dice, p1r1best);

        InfoSet p2r0 = getInfoSet(p2state);
        int p2r0best = p2r0.bestChild();
        p2r0.visits++;
        p2r0.childStats[p2r0best].visit(p2r0.visits);
        p2state.dice = rollDice(p2state.dice, p2r0best);
        p2state.round++;

        InfoSet p2r1 = getInfoSet(p2state);
        int p2r1best = p2r1.bestChild();
        p2r1.visits++;
        p2r1.childStats[p2r1best].visit(p2r1.visits);
        p2state.dice = rollDice(p2state.dice, p2r1best);

        if (battleResults[p1state.dice][p2state.dice] == P1_WIN) {
            p1r0.childStats[p1r0best].win(p1r0.visits);
            p1r1.childStats[p1r1best].win(p1r1.visits);
        } else if (battleResults[p1state.dice][p2state.dice] == P2_WIN) {
            p2r0.childStats[p2r0best].win(p2r0.visits);
            p2r1.childStats[p2r1best].win(p2r1.visits);
        }
    }

    public static InfoSet getInfoSet(InfoSet state) {
        if (!infoSets.containsKey(state.hashCode())) {
            infoSets.put(state.hashCode(), new InfoSet(state.round, state.attacker, state.me, state.them, state.dice, keepPossibilities[state.dice].length));
        }
        return infoSets.get(state.hashCode());
    }
}
