public class Stats {
    public static final double C = 0.5;

    public int visits;
    public int wins;
    public double score = 1.0;

    public void visit(int parentVisits) {
        visits++;
        score = (double)wins / visits + 0.5 * Math.sqrt(Math.log(parentVisits) / visits);
    }

    public void win(int parentVisits) {
        wins++;
        score = (double)wins / visits + 0.5 * Math.sqrt(Math.log(parentVisits) / visits);
    }
}
