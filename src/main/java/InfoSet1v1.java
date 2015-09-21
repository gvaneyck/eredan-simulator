public class InfoSet1v1 {
    public int phase;
    public boolean attacker;
    public int me;
    public int them;
    public int dice;

    public int visits;
    public Stats[] childStats;

    public InfoSet1v1(int phase, boolean attacker, int me, int them, int dice, int nChildren) {
        this.phase = phase;
        this.attacker = attacker;
        this.me = me;
        this.them = them;
        this.dice = dice;

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

    public int hashCode() {
        return (me << 18)
                + (them << 10)
                + (attacker ? 1 << 9 : 0)
                + (phase << 7)
                + dice;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof InfoSet1v1) {
            InfoSet1v1 infoSet = (InfoSet1v1)o;
            return (infoSet.phase == phase && infoSet.me == me && infoSet.dice == dice);
        } else {
            return false;
        }
    }
}
