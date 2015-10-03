package eredan;

import eredan.simulator.BattleData;
import eredan.simulator.CharacterStatus;
import eredan.simulator.Dice;
import eredan.simulator.Heroes;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/*
TODO: Performance
10.5 TeamState.hashCode
8.4 EredanSimulator.getNodeStats
8.4 CharacterStatus.copy
7.7 CharacterStatus.equals
7.7 TeamState.copy
7.7 TeamState.equals
4.6 CharacterStatus.hashCode
 */

public class EredanSimulator {

    public static final int GENERATIONS = 1000;
    public static final int SIMULATIONS = 10000;

    public static Logger log = Logger.getLogger("foo");
    public static Random rand = new Random();

    public static Map<TeamState, NodeStats> teamStats = new HashMap<>(10000000);
    public static Map<String, Boolean> recordedResults = new HashMap<>();

    protected static class WeightedList {
        int idx;
        int weight;
        List<Integer> list;
        public WeightedList(int idx, int weight, List<Integer> list) {
            this.idx = idx;
            this.weight = weight;
            this.list = list;
        }
    }

    public static void main(String[] args) {
        // For profiling
//        try { System.in.read(); } catch (Exception e) { }

        for (int i = 0; i < Heroes.heroes.size(); i++) {
            System.out.println(i + " " + Heroes.heroes.get(i).name);
        }
        System.out.println();

//        testChallenger();
        runGeneticAlgorithm();
    }

    public static void testChallenger() {
        List<List<Integer>> teams = new ArrayList<>();
        List<Integer> team;
        team = new ArrayList<>(); team.add(11); team.add(27); team.add(76); team.add(66); team.add(89); teams.add(team);
        team = new ArrayList<>(); team.add(15); team.add(33); team.add(46); team.add(65); team.add(76); teams.add(team);

        // Hate, Carkasse, Kitsana, Amidaraxar, and Lania
        List<Integer> challenger;
        challenger = new ArrayList<>(); challenger.add(66); challenger.add(63); challenger.add(54); challenger.add(81); challenger.add(90);

        int sims = 300000;
        double winRate;
        for (int k = 0; k < teams.size(); k++) {
            for (int i = 0; i < sims; i++) {
                runSimTeam(challenger, teams.get(k));
            }
            summarizeStats();

            winRate = getWinRateGr50();
            if (winRate > 0.5) {
                System.out.println(String.format("chal VS %d | chal %.1f", k, winRate * 100));
            } else {
                System.out.println(String.format("chal VS %d | %d %.1f", k, k, (1 - winRate) * 100));
            }
            winRate = getWinRateAvg();
            if (winRate > 0.5) {
                System.out.println(String.format("chal VS %d | chal %.1f", k, winRate * 100));
            } else {
                System.out.println(String.format("chal VS %d | %d %.1f", k, k, (1 - winRate) * 100));
            }

            teamStats.clear();

            for (int i = 0; i < sims; i++) {
                runSimTeam(teams.get(k), challenger);
            }
            summarizeStats();

            winRate = getWinRateGr50();
            if (winRate > 0.5) {
                System.out.println(String.format("%d VS chal | %d %.1f", k, k, winRate * 100));
            } else {
                System.out.println(String.format("%d VS chal | chal %.1f", k, (1 - winRate) * 100));
            }
            winRate = getWinRateAvg();
            if (winRate > 0.5) {
                System.out.println(String.format("%d VS chal | %d %.1f", k, k, winRate * 100));
            } else {
                System.out.println(String.format("%d VS chal | chal %.1f", k, (1 - winRate) * 100));
            }

            teamStats.clear();
        }
    }

    public static void runGeneticAlgorithm() {
        List<List<Integer>> teams = new ArrayList<>();
        List<Integer> team;
        for (int i = 0; i < 23; i++) {
            team = new ArrayList<>();
            team.add(i*5);
            team.add(i*5+1);
            team.add(i*5+2);
            team.add(i*5+3);
            team.add(i*5+4);
            teams.add(team);
        }

        for (int i = 0; i < GENERATIONS; i++) {
            int[] teamWins = runRoundRobin(teams);

            List<WeightedList> wlist = new ArrayList<>();
            for (int k = 0; k < teams.size(); k++) {
                wlist.add(new WeightedList(k, teamWins[k], teams.get(k)));
            }
            Collections.sort(wlist, (e1, e2) -> Integer.compare(e2.weight, e1.weight));

            teams.clear();

            // Keep top 5
            for (int k = 0; k < 5; k++) {
                teams.add(wlist.get(k).list);
                System.out.println(wlist.get(k).idx + " | " + teams.get(k));
            }

            // +4 mutations of each
            for (int k = 0; k < 5; k++) {
                List<Integer> sourceTeam = wlist.get(k).list;
                mutateUniqueAndAdd(teams, new ArrayList<>(sourceTeam), 2);
                mutateUniqueAndAdd(teams, new ArrayList<>(sourceTeam), 3);
                mutateUniqueAndAdd(teams, new ArrayList<>(sourceTeam), 4);
                mutateUniqueAndAdd(teams, new ArrayList<>(sourceTeam), 5);
            }

            // And shuffle to avoid stale, many-way ties at top (lower index is tiebreaker)
            Collections.shuffle(teams);
        }
    }

    public static int[] runRoundRobin(List<List<Integer>> teams) {
        int[] teamWins = new int[teams.size()];
        for (int t1 = 0; t1 < teams.size(); t1++) {
            for (int t2 = 0; t2 < teams.size(); t2++) {
                if (t1 == t2) {
                    continue;
                }

                System.out.print(t1 + " VS " + t2);
                String matchKey = String.format("%d|%d|%d|%d|%d|%d|%d|%d|%d|%d",
                        teams.get(t1).get(0),
                        teams.get(t1).get(1),
                        teams.get(t1).get(2),
                        teams.get(t1).get(3),
                        teams.get(t1).get(4),
                        teams.get(t2).get(0),
                        teams.get(t2).get(1),
                        teams.get(t2).get(2),
                        teams.get(t2).get(3),
                        teams.get(t2).get(4));

                // Commented out to re-run simulations and potentially get a different winner
//                if (recordedResults.containsKey(matchKey)) {
//                    if (recordedResults.get(matchKey)) {
//                        System.out.println(" | " + t1);
//                        teamWins[t1]++;
//                    } else {
//                        System.out.println(" | " + t2);
//                        teamWins[t2]++;
//                    }
//                } else {
                    for (int i = 0; i < SIMULATIONS; i++) {
                        runSimTeam(teams.get(t1), teams.get(t2));
                    }

                    double winRate = getWinRateGr50();
                    if (winRate > 0.5) {
                        System.out.println(String.format(" | %d %.1f", t1, winRate * 100));
                        teamWins[t1]++;
                        recordedResults.put(matchKey, true);
                    } else {
                        System.out.println(String.format(" | %d %.1f", t2, (1 - winRate) * 100));
                        teamWins[t2]++;
                        recordedResults.put(matchKey, false);
                    }

                    teamStats.clear();
//                }
            }
        }

        // For testing one round robin with varying simulation counts
        for (int i = 0; i < teamWins.length; i++) {
            System.out.println(teamWins[i]);
        }
        System.exit(0);

        return teamWins;
    }

    // % of best choices > 50%
    public static double getWinRateGr50() {
        int wins = 0;
        int visits = 0;
        for (TeamState ts : teamStats.keySet()) {
            if (ts.round == 0 && ts.me == null && ts.them == null) {
                double bestScore = -1;

                NodeStats stats = teamStats.get(ts);
                for (int q = 0; q < stats.childStats.length; q++) {
                    double tempScore = stats.getChildWinRate(q);
                    if (tempScore > bestScore) {
                        bestScore = tempScore;
                    }
                }

                if (bestScore > 0.5) {
                    wins++;
                }
                visits++;
            }
        }

        return (double) wins / visits;
    }

    // Average of best choices
    public static double getWinRateAvg() {
        int wins = 0;
        int visits = 0;
        for (TeamState ts : teamStats.keySet()) {
            if (ts.round == 0 && ts.me == null && ts.them == null) {
                int bestWins = 0;
                int bestVisits = 0;
                double bestScore = -1;

                NodeStats stats = teamStats.get(ts);
                for (int q = 0; q < stats.childStats.length; q++) {
                    double tempScore = stats.getChildWinRate(q);
                    if (tempScore > bestScore) {
                        bestScore = tempScore;
                        bestWins = stats.childStats[q].wins;
                        bestVisits = stats.visits;
                    }
                }

                wins += bestWins;
                visits += bestVisits;
            }
        }

        return (double) wins / visits;
    }

    public static void mutateUniqueAndAdd(List<List<Integer>> teams, List<Integer> team, int n) {
        mutate(team, n);
        while (teams.contains(team)) {
            mutate(team, 1);
        }
        teams.add(team);
    }

    public static void mutate(List<Integer> team, int n) {
        Collections.shuffle(team);
        for (int i = 0; i < n; i++) {
            team.remove(rand.nextInt(5));
            int newTeammate;
            do {
                newTeammate = rand.nextInt(Heroes.heroes.size());
            } while (team.contains(newTeammate));
            team.add(newTeammate);
        }
        Collections.sort(team);
    }

    public static void summarizeStats() {
        Map<String, String> results = new HashMap<>();
        for (TeamState ts : teamStats.keySet()) {
            NodeStats stats = teamStats.get(ts);
            if (ts.round == 0 && ts.me == null && ts.them == null) {
                String key = String.format("%s %s %s VS %s %s %s",
                        ts.allies[0].name.substring(0, 5),
                        ts.allies[1].name.substring(0, 5),
                        ts.allies[2].name.substring(0, 5),
                        ts.enemies[0].name.substring(0, 5),
                        ts.enemies[1].name.substring(0, 5),
                        ts.enemies[2].name.substring(0, 5));

                String value = buildStatsString(stats);
                value += String.format(" | %5s %5s %5s",
                        String.format("%.1f", (stats.childStats[0] == null ? -1 : (double)stats.childStats[0].wins / stats.visits * 100)),
                        String.format("%.1f", (stats.childStats[1] == null ? -1 : (double)stats.childStats[1].wins / stats.visits * 100)),
                        String.format("%.1f", (stats.childStats[2] == null ? -1 : (double)stats.childStats[2].wins / stats.visits * 100)));

                results.put(key, value);
            }
        }

        results.entrySet().stream().sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey())).forEach(e -> log.debug(e.getValue() + " " + e.getKey()));
    }

    public static String buildStatsString(NodeStats stats) {
        int bestWins = 0;
        int bestVisits = 0;
        double bestScore = -1;
        for (int i = 0; i < stats.childStats.length; i++) {
            double tempScore = stats.getChildWinRate(i);
            if (tempScore > bestScore) {
                bestScore = tempScore;
                bestWins = stats.childStats[i].wins;
                bestVisits = stats.visits;
            }
        }

        return String.format("%5d / %5d = %5s", bestWins, bestVisits, String.format("%.1f", bestScore * 100));
    }

    public static void runSimTeam(List<Integer> team1, List<Integer> team2) {
        CharacterStatus[] team1cs = new CharacterStatus[5];
        for (int i = 0; i < team1.size(); i++) {
            team1cs[i] = new CharacterStatus(Heroes.heroes.get(team1.get(i)));
        }

        CharacterStatus[] team2cs = new CharacterStatus[5];
        for (int i = 0; i < team2.size(); i++) {
            team2cs[i] = new CharacterStatus(Heroes.heroes.get(team2.get(i)));
        }

        Util.shuffleArray(team1cs);
        Util.shuffleArray(team2cs);

        TeamState p1state = new TeamState(team1cs);
        p1state.attacker = true;

        TeamState p2state = new TeamState(team2cs);

        p1state.enemies = p2state.allies;
        p2state.enemies = p1state.allies;

        List<NodeStats> p1stats = new ArrayList<>(20);
        List<NodeStats> p2stats = new ArrayList<>(20);

        while (p1state.myWins < 3 && p1state.theirWins < 3 && p1state.round < 5) {
            int p1pick, p2pick;
            if (p1state.attacker) {
                // Attacker picks
                NodeStats p1pickStats = getNodeStats(p1state);
                p1stats.add(p1pickStats);
                p1pick = p1pickStats.visitBest();

                p1state.me = p1state.allies[p1pick];
                p1state.allies[p1pick] = null;
                p2state.them = p1state.me;

                // Defender picks
                NodeStats p2pickStats = getNodeStats(p2state);
                p2stats.add(p2pickStats);
                p2pick = p2pickStats.visitBest();

                p2state.me = p2state.allies[p2pick];
                p2state.allies[p2pick] = null;
                p1state.them = p2state.me;
            } else {
                // Attacker picks
                NodeStats p2pickStats = getNodeStats(p2state);
                p2stats.add(p2pickStats);
                p2pick = p2pickStats.visitBest();

                p2state.me = p2state.allies[p2pick];
                p2state.allies[p2pick] = null;
                p1state.them = p2state.me;

                // Defender picks
                NodeStats p1pickStats = getNodeStats(p1state);
                p1stats.add(p1pickStats);
                p1pick = p1pickStats.visitBest();

                p1state.me = p1state.allies[p1pick];
                p1state.allies[p1pick] = null;
                p2state.them = p1state.me;
            }

            // P1 dice rolling
            p1state.dice = Dice.roll(0, 0); // Random roll

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
            p2state.dice = Dice.roll(0, 0); // Random roll

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

            // Set allies on battlers
            int pos = 0;
            for (int i = 0; i < 3; i++) {
                if (p1state.allies[i] != null) {
                    p1state.me.allies[pos] = p1state.allies[i];
                    pos++;
                }
            }

            pos = 0;
            for (int i = 0; i < 3; i++) {
                if (p2state.allies[i] != null) {
                    p2state.me.allies[pos] = p2state.allies[i];
                    pos++;
                }
            }

            // Get battle result
            if (p1state.attacker) {
                int result = BattleData.simulate(p1state.me, p2state.me, p1state.dice, p2state.dice);
                if (result == BattleData.P1_WIN) {
                    p1state.myWins++;
                    p2state.theirWins++;
                } else if (result == BattleData.P2_WIN) {
                    p2state.myWins++;
                    p1state.theirWins++;
                }
            } else {
                int result = BattleData.simulate(p2state.me, p1state.me, p2state.dice, p1state.dice);
                if (result == BattleData.P1_WIN) {
                    p2state.myWins++;
                    p1state.theirWins++;
                } else if (result == BattleData.P2_WIN) {
                    p1state.myWins++;
                    p2state.theirWins++;
                }
            }

            // Draw ally
            if (p1state.round < 2) {
                p1state.replaceAlly(p1pick, team1cs[3 + p1state.round]);
                p2state.replaceAlly(p2pick, team2cs[3 + p1state.round]);
            } else {
                p1state.sortAllies();
                p2state.sortAllies();
            }

            // Reset state
            p1state.dice = 0;
            p1state.me = null;
            p1state.them = null;
            p1state.attacker = !p1state.attacker;
            p1state.round++;

            p2state.dice = 0;
            p2state.me = null;
            p2state.them = null;
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
        NodeStats result = teamStats.get(state);
        if (result == null) {
            result = new NodeStats();
            if (state.me == null) {
                result.generateChildren(Math.min(3, 5 - state.round));
            } else {
                result.generateChildren(Dice.keeps[state.dice].length);
            }

            teamStats.put(state.copy(), result);
        }
        return result;
    }
}
