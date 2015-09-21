public class NodeStats {
    public int lastVisit;
    public int visits;
    public Stats[] childStats;

    public void generateChildren(int nChildren) {
        childStats = new Stats[nChildren];
        for (int i = 0; i < nChildren; i++) {
            childStats[i] = new Stats();
        }
    }

    public int bestChild() {
        double bestScore = -1;
        int idx = 0;
        for (int i = 0; i < childStats.length; i++) {
            if (childStats[i].score > bestScore) {
                bestScore = childStats[i].score;
                idx = i;
            }
        }
        return idx;
    }

    public int visitBest() {
        lastVisit = bestChild();
        visits++;
        childStats[lastVisit].visit(visits);
        return lastVisit;
    }

    public void addWin() {
        childStats[lastVisit].win(visits);
    }
}
