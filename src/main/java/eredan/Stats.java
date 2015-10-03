package eredan;

public class Stats {
    public static final double C = 0.5;

    public int visits;
    public int wins;
    public double score = 1.0;

    public double getScore(int parentVisits) {
        return (double)wins / visits + C * Math.sqrt(Math.log(parentVisits) / visits);
    }
}
