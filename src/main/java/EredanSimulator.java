import org.apache.log4j.Logger;
import simulator.BattleData;
import simulator.CharacterStatus;
import simulator.Dice;
import simulator.Heroes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class EredanSimulator {

    public static Logger log = Logger.getLogger("foo");
    public static Random rand = new Random();

    public static Map<TeamState, NodeStats> teamStats = new HashMap<>();

    public static void main(String[] args) {
        List<Integer> team1 = new ArrayList<>();
        team1.add(0); team1.add(1); team1.add(2); team1.add(3); team1.add(4);
        List<Integer> team2 = new ArrayList<>();
        team2.add(5); team2.add(6); team2.add(7); team2.add(8); team2.add(12);

        int runs = 10000000;
        for (int i = 0; i < runs; i++) {
            if (i % 10000 == 0) {
                log.debug(String.format("%.1f", (double) i / runs * 100));
            }
            runSimTeam(team1, team2);
        }

        Map<String, String> results = new HashMap<>();
        for (TeamState ts : teamStats.keySet()) {
            if (ts.round == 0 && ts.me == null && ts.them == null) {
                NodeStats stats = teamStats.get(ts);
                int bestWins = 0;
                int bestVisits = 0;
                double bestScore = -1;
                for (int i = 0; i < stats.childStats.length; i++) {
                    double tempScore = (double)stats.childStats[i].wins / stats.visits;
                    if (tempScore > bestScore) {
                        bestScore = tempScore;
                        bestWins = stats.childStats[i].wins;
                        bestVisits = stats.visits;
                    }
                }

                String value = String.format("%5d / %5d = %.1f ", bestWins, bestVisits, bestScore * 100);
                String key = String.format("%s %s %s VS %s %s %s",
                        ts.allies.get(0).name.substring(0, 5),
                        ts.allies.get(1).name.substring(0, 5),
                        ts.allies.get(2).name.substring(0, 5),
                        ts.enemies.get(0).name.substring(0, 5),
                        ts.enemies.get(1).name.substring(0, 5),
                        ts.enemies.get(2).name.substring(0, 5));

                results.put(key, value);
            }
        }

        results.entrySet().stream().sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey())).forEach(e -> log.debug(e.getValue() + " " + e.getKey()));
    }

    public static void mcts(int h1, int h2) {
//        for (int i = 0; i < 1000000; i++) {
//            runSimTeam(h1, h2);
//        }
//
//        double p1win = 0, p2win = 0;
//        int p1sets = 0, p2sets = 0;
//        for (int key : infoSets1v1.keySet()) {
//            InfoSet1v1 set = infoSets1v1.get(key);
//            if (set.me == h1 && set.them == h2 && set.attacker && set.phase == 0) {
//                p1win += (double)set.childStats[set.bestChild()].wins / set.childStats[set.bestChild()].visits;
//                p1sets++;
//            } else if (set.me == h2 && set.them == h1 && !set.attacker && set.phase == 0) {
//                p2win += (double)set.childStats[set.bestChild()].wins / set.childStats[set.bestChild()].visits;
//                p2sets++;
//            }
//        }
//
//        log.debug(Heroes.heroes.get(h1).name + " VS " + Heroes.heroes.get(h2).name);
//        log.debug((p1win / p1sets * 100) + " VS " + (p2win / p2sets * 100));
    }

    public static void runSimTeam(List<Integer> team1, List<Integer> team2) {
        List<CharacterStatus> team1cs = new ArrayList<>();
        for (int i : team1) {
            team1cs.add(new CharacterStatus(Heroes.heroes.get(i)));
        }

        List<CharacterStatus> team2cs = new ArrayList<>();
        for (int i : team2) {
            team2cs.add(new CharacterStatus(Heroes.heroes.get(i)));
        }

        Collections.shuffle(team1cs);
        Collections.shuffle(team2cs);

        TeamState p1state = new TeamState(team1cs);
        p1state.attacker = true;

        TeamState p2state = new TeamState(team2cs);

        p1state.enemies = p2state.allies;
        p2state.enemies = p1state.allies;

        List<NodeStats> p1stats = new ArrayList<>();
        List<NodeStats> p2stats = new ArrayList<>();

        while (p1state.myWins < 3 && p1state.theirWins < 3 && p1state.round < 5) {
            if (p1state.attacker) {
                // Attacker picks
                NodeStats p1pickStats = getNodeStats(p1state);
                p1stats.add(p1pickStats);
                int p1pick = p1pickStats.visitBest();

                p1state.me = p1state.allies.remove(p1pick);
                p2state.them = p1state.me;

                // Defender picks
                NodeStats p2pickStats = getNodeStats(p2state);
                p2stats.add(p2pickStats);
                int p2pick = p2pickStats.visitBest();

                p2state.me = p2state.allies.remove(p2pick);
                p1state.them = p2state.me;
            } else {
                // Attacker picks
                NodeStats p2pickStats = getNodeStats(p2state);
                p2stats.add(p2pickStats);
                int p2pick = p2pickStats.visitBest();

                p2state.me = p2state.allies.remove(p2pick);
                p1state.them = p2state.me;

                // Defender picks
                NodeStats p1pickStats = getNodeStats(p1state);
                p1stats.add(p1pickStats);
                int p1pick = p1pickStats.visitBest();

                p1state.me = p1state.allies.remove(p1pick);
                p2state.them = p1state.me;
            }

            // P1 dice rolling
            p1state.dice = rand.nextInt(84);

            NodeStats p1roll1set = getNodeStats(p1state);
            p1stats.add(p1roll1set);
            int p1roll1 = p1roll1set.visitBest();

            p1state.dice = Dice.roll(p1state.dice, p1roll1);
            p1state.phase = 1;

            NodeStats p1roll2set = getNodeStats(p1state);
            p1stats.add(p1roll2set);
            int p1roll2 = p1roll2set.visitBest();

            p1state.dice = Dice.roll(p1state.dice, p1roll2);
            p1state.phase = 0;

            // P2 dice rolling
            p2state.dice = rand.nextInt(84);

            NodeStats p2roll1set = getNodeStats(p2state);
            p2stats.add(p2roll1set);
            int p2roll1 = p2roll1set.visitBest();

            p2state.dice = Dice.roll(p2state.dice, p2roll1);
            p2state.phase = 1;

            NodeStats p2roll2set = getNodeStats(p2state);
            p2stats.add(p2roll2set);
            int p2roll2 = p2roll2set.visitBest();

            p2state.dice = Dice.roll(p2state.dice, p2roll2);
            p2state.phase = 0;

            if (p1state.allies.size() > 0) p1state.me.ally1 = p1state.allies.get(0);
            if (p1state.allies.size() > 1) p1state.me.ally2 = p1state.allies.get(1);

            if (p2state.allies.size() > 0) p2state.me.ally1 = p2state.allies.get(0);
            if (p2state.allies.size() > 1) p2state.me.ally2 = p2state.allies.get(1);

            int result = BattleData.simulate(p1state.me, p2state.me, p1state.dice, p2state.dice);
            if (result == BattleData.P1_WIN) {
                p1state.myWins++;
                p2state.theirWins++;
            } else if (result == BattleData.P2_WIN) {
                p2state.myWins++;
                p1state.theirWins++;
            }

            p1state.dice = 0;
            p1state.me = null;
            p1state.them = null;
            if (team1cs.size() > 0) { p1state.addAlly(team1cs.remove(0)); }
            p1state.attacker = !p1state.attacker;
            p1state.round++;

            p2state.dice = 0;
            p2state.me = null;
            p2state.them = null;
            if (team2cs.size() > 0) { p2state.addAlly(team2cs.remove(0)); }
            p2state.attacker = !p2state.attacker;
            p2state.round++;
        }

        if (p1state.myWins > p2state.myWins) {
            for (NodeStats ns : p1stats) {
                ns.addWin();
            }
        } else if (p1state.myWins < p2state.myWins) {
            for (NodeStats ns : p2stats) {
                ns.addWin();
            }
        }
    }

    public static NodeStats getNodeStats(TeamState state) {
        if (!teamStats.containsKey(state)) {
            TeamState key = state.copy();
            NodeStats stats = new NodeStats();
            if (state.me == null) {
                stats.generateChildren(Math.min(3, 5 - state.round));
            } else {
                stats.generateChildren(Dice.keeps[state.dice].length);
            }

            teamStats.put(key, stats);
        }
        return teamStats.get(state);
    }
}
