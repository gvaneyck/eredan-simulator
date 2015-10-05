package eredan;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NodeStats {

    public static final double C = 0.5;
    public static final double BASE_VALUE = 0.8;

    public static Random rand = new Random();

    public int lastVisit;
    public int visits;
    public Stats[] childStats;

    public void generateChildren(int nChildren) {
        childStats = new Stats[nChildren];
    }

    public int bestChild() {
        List<Integer> ties = new ArrayList<>(36);
        double bestScore = -1;
        for (int i = 0; i < childStats.length; i++) {
            double score;
            if (childStats[i] == null) {
                score = BASE_VALUE;
            } else {
                score = (double)childStats[i].wins / childStats[i].visits + C * Math.sqrt(Math.log(visits) / childStats[i].visits);
            }

            if (score >= bestScore) {
                if (score > bestScore) {
                    bestScore = score;
                    ties.clear();
                }
                ties.add(i);
            }
        }

        if (ties.size() == 1) {
            return ties.get(0);
        } else {
            return ties.get(rand.nextInt(ties.size()));
        }
    }

    public int visitBest() {
        lastVisit = bestChild();
        visits++;
        if (childStats[lastVisit] == null) {
            childStats[lastVisit] = new Stats();
        }
        childStats[lastVisit].visits++;
        return lastVisit;
    }

    public void addWin() {
        childStats[lastVisit].wins++;
    }

    public double getChildWinRate(int i) {
        return (childStats[i] != null ? (double)childStats[i].wins / visits : -1);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(visits);
        for (int i = 0; i < childStats.length; i++) {
            sb.append(' ');
            sb.append(i);
            sb.append(':');
            if (childStats[i] == null) {
                sb.append("0/0");
            } else {
                sb.append(childStats[i].wins);
                sb.append('/');
                sb.append(childStats[i].visits);
            }
        }
        return sb.toString();
    }
}
