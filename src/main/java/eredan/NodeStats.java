package eredan;

public class NodeStats {
    public int lastVisit;
    public int visits;
    public Stats[] childStats;

    public void generateChildren(int nChildren) {
        childStats = new Stats[nChildren];
    }

    public int bestChild() {
        double bestScore = -1;
        int idx = 0;
        for (int i = 0; i < childStats.length; i++) {
            double score = (childStats[i] != null ? childStats[i].score : 1);
            if (score > bestScore) {
                bestScore = score;
                idx = i;
            }
        }
        return idx;
    }

    public int visitBest() {
        lastVisit = bestChild();
        visits++;
        if (childStats[lastVisit] == null) {
            childStats[lastVisit] = new Stats();
        }
        childStats[lastVisit].visit(visits);
        return lastVisit;
    }

    public void addWin() {
        childStats[lastVisit].win(visits);
    }

    public double getChildWinRate(int i) {
        return (childStats[i] != null ? (double)childStats[i].wins / visits : -1);
    }
}
