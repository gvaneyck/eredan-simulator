package eredan;

import org.apache.log4j.Logger;
import eredan.simulator.BattleData;
import eredan.simulator.CharacterStatus;
import eredan.simulator.Dice;
import eredan.simulator.Heroes;

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

    public static Logger log = Logger.getLogger("foo");
    public static Random rand = new Random();

    public static Map<TeamState, NodeStats> teamStats = new HashMap<>(1000000);

    protected static class WeightedList {
        int weight;
        List<Integer> list;
        public WeightedList(int weight, List<Integer> list) {
            this.weight = weight;
            this.list = list;
        }
    }

    public static void main(String[] args) {
//        try { System.in.read(); } catch (Exception e) { }
        List<List<Integer>> teams = new ArrayList<>();

        List<Integer> team;
        for (int i = 0; i < 5; i++) {
            team = new ArrayList<>();
            team.add(i*5);
            team.add(i*5+1);
            team.add(i*5+2);
            team.add(i*5+3);
            team.add(i*5+4);
            teams.add(team);
        }

        for (int i = 0; i < 5; i++) {
            mutateUniqueAndAdd(teams, new ArrayList<>(teams.get(i)), 5);
            mutateUniqueAndAdd(teams, new ArrayList<>(teams.get(i)), 5);
            mutateUniqueAndAdd(teams, new ArrayList<>(teams.get(i)), 5);
        }

        for (int i = 0; i < 100; i++) {
            for (int k = 0; k < teams.size(); k++) {
                System.out.println(k + " | " + teams.get(k));
            }

            int[] teamWins = runRoundRobin(teams);

            List<WeightedList> wlist = new ArrayList<>();
            for (int k = 0; k < teams.size(); k++) {
                wlist.add(new WeightedList(teamWins[k], teams.get(k)));
            }
            Collections.sort(wlist, (e1, e2) -> Integer.compare(e2.weight, e1.weight));

            teams.clear();

            // Keep top 5
            for (int k = 0; k < 5; k++) {
                teams.add(wlist.get(k).list);
            }

            // +4 mutations of each
            for (int k = 0; k < 4; k++) {
                List<Integer> sourceTeam = wlist.get(k).list;
                mutateUniqueAndAdd(teams, new ArrayList<>(sourceTeam), 2);
                mutateUniqueAndAdd(teams, new ArrayList<>(sourceTeam), 3);
                mutateUniqueAndAdd(teams, new ArrayList<>(sourceTeam), 4);
                mutateUniqueAndAdd(teams, new ArrayList<>(sourceTeam), 5);
            }
        }
    }

    public static int[] runRoundRobin(List<List<Integer>> teams) {
        int runs = 100000;
        int[] teamWins = new int[teams.size()];
        for (int t1 = 0; t1 < teams.size(); t1++) {
            for (int t2 = 0; t2 < teams.size(); t2++) {
                if (t1 == t2) {
                    continue;
                }

                System.out.print(t1 + " VS " + t2);
                for (int i = 0; i < runs; i++) {
                    runSimTeam(teams.get(t1), teams.get(t2));
                }

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

                double winRate = (double)wins / visits;
                if (winRate > 0.5) {
                    System.out.println(String.format(" | %d %.1f", t1, winRate * 100));
                    teamWins[t1]++;
                } else {
                    System.out.println(String.format(" | %d %.1f", t2, (1 - winRate) * 100));
                    teamWins[t2]++;
                }

                teamStats.clear();
            }
        }

        return teamWins;
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
        Map<String, String> oppresults = new HashMap<>();
        for (TeamState ts : teamStats.keySet()) {
            NodeStats stats = teamStats.get(ts);
            if (ts.round == 0 && ts.me == null && ts.them == null) {
                String key = String.format("%s %s %s VS %s %s %s",
                        ts.allies.get(0).name.substring(0, 5),
                        ts.allies.get(1).name.substring(0, 5),
                        ts.allies.get(2).name.substring(0, 5),
                        ts.enemies.get(0).name.substring(0, 5),
                        ts.enemies.get(1).name.substring(0, 5),
                        ts.enemies.get(2).name.substring(0, 5));

                String value = buildStatsString(stats);
                value += String.format(" | %5s %5s %5s",
                        String.format("%.1f", (double)stats.childStats[0].wins / stats.visits * 100),
                        String.format("%.1f", (double)stats.childStats[1].wins / stats.visits * 100),
                        String.format("%.1f", (double)stats.childStats[2].wins / stats.visits * 100));

                results.put(key, value);
            } else if (ts.round == 0 && ts.me == null && ts.them != null) {
                String key = String.format("%s %s %s VS (%s) %s %s",
                        ts.allies.get(0).name.substring(0, 5),
                        ts.allies.get(1).name.substring(0, 5),
                        ts.allies.get(2).name.substring(0, 5),
                        ts.them.name.substring(0, 5),
                        ts.enemies.get(0).name.substring(0, 5),
                        ts.enemies.get(1).name.substring(0, 5));

                oppresults.put(key, buildStatsString(stats));
            }
        }

        results.entrySet().stream().sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey())).forEach(e -> log.debug(e.getValue() + " " + e.getKey()));
        log.debug("----------");
        oppresults.entrySet().stream().sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey())).forEach(e -> log.debug(e.getValue() + " " + e.getKey()));
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
