import org.apache.log4j.Logger;
import simulator.BattleData;
import simulator.Dice;
import simulator.Heroes;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EredanSimulatorSingle {

    public static Logger log = Logger.getLogger("foo");
    public static Random rand = new Random();
    public static Map<Integer, InfoSet1v1> infoSets1v1 = new HashMap<>();

    public static void main(String[] args) {
        mcts(0, 1);
    }

    public static void mcts(int h1, int h2) {
        for (int i = 0; i < 1000000; i++) {
            runSim1v1(h1, h2);
        }

        double p1win = 0, p2win = 0;
        int p1sets = 0, p2sets = 0;
        for (int key : infoSets1v1.keySet()) {
            InfoSet1v1 set = infoSets1v1.get(key);
            if (set.me == h1 && set.them == h2 && set.attacker && set.phase == 0) {
                p1win += (double)set.childStats[set.bestChild()].wins / set.childStats[set.bestChild()].visits;
                p1sets++;
            } else if (set.me == h2 && set.them == h1 && !set.attacker && set.phase == 0) {
                p2win += (double)set.childStats[set.bestChild()].wins / set.childStats[set.bestChild()].visits;
                p2sets++;
            }
        }

        log.debug(Heroes.heroes.get(h1).name + " VS " + Heroes.heroes.get(h2).name);
        log.debug((p1win / p1sets * 100) + " VS " + (p2win / p2sets * 100));
    }

    public static void runSim1v1(int h1, int h2) {
        // Transient state used to look up real stat tracking infosets
        InfoSet1v1 p1state = new InfoSet1v1(0, true, h1, h2, rand.nextInt(84), 0);
        InfoSet1v1 p2state = new InfoSet1v1(0, false, h2, h1, rand.nextInt(84), 0);

        InfoSet1v1 p1r0 = getInfoSet1v1(p1state);
        int p1r0best = p1r0.bestChild();
        p1r0.visits++;
        p1r0.childStats[p1r0best].visit(p1r0.visits);
        p1state.dice = Dice.roll(p1state.dice, p1r0best);
        p1state.phase++;

        InfoSet1v1 p1r1 = getInfoSet1v1(p1state);
        int p1r1best = p1r1.bestChild();
        p1r1.visits++;
        p1r1.childStats[p1r1best].visit(p1r1.visits);
        p1state.dice = Dice.roll(p1state.dice, p1r1best);

        InfoSet1v1 p2r0 = getInfoSet1v1(p2state);
        int p2r0best = p2r0.bestChild();
        p2r0.visits++;
        p2r0.childStats[p2r0best].visit(p2r0.visits);
        p2state.dice = Dice.roll(p2state.dice, p2r0best);
        p2state.phase++;

        InfoSet1v1 p2r1 = getInfoSet1v1(p2state);
        int p2r1best = p2r1.bestChild();
        p2r1.visits++;
        p2r1.childStats[p2r1best].visit(p2r1.visits);
        p2state.dice = Dice.roll(p2state.dice, p2r1best);

        int result = BattleData.simulate(h1, h2, p1state.dice, p2state.dice);
        if (result == BattleData.P1_WIN) {
            p1r0.childStats[p1r0best].win(p1r0.visits);
            p1r1.childStats[p1r1best].win(p1r1.visits);
        } else if (result == BattleData.P2_WIN) {
            p2r0.childStats[p2r0best].win(p2r0.visits);
            p2r1.childStats[p2r1best].win(p2r1.visits);
        }
    }

    public static InfoSet1v1 getInfoSet1v1(InfoSet1v1 state) {
        if (!infoSets1v1.containsKey(state.hashCode())) {
            infoSets1v1.put(state.hashCode(), new InfoSet1v1(state.phase, state.attacker, state.me, state.them, state.dice, Dice.keeps[state.dice].length));
        }
        return infoSets1v1.get(state.hashCode());
    }
}
