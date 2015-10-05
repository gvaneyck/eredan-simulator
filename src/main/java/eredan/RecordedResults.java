package eredan;

import java.util.HashMap;
import java.util.Map;

public class RecordedResults {
    Map<String, Map<Integer, Double>> fiftyPctStats = new HashMap<>();
    Map<String, Map<Integer, Double>> winRateStats = new HashMap<>();

    public int getLargestSim(String key) {
        Map<Integer, Double> stats = fiftyPctStats.get(key);
        if (stats == null) {
            return 1000;
        } else if (stats.containsKey(1000000)) {
            return 1000000;
        } else if (stats.containsKey(100000)) {
            return 100000;
        } else if (stats.containsKey(10000)) {
            return 10000;
        } else {
            return 1000;
        }
    }

    public void record(String key, int sims, double fiftyPct, double winRate) {
        if (!fiftyPctStats.containsKey(key)) {
            fiftyPctStats.put(key, new HashMap<>());
        }
        if (!winRateStats.containsKey(key)) {
            winRateStats.put(key, new HashMap<>());
        }
        fiftyPctStats.get(key).put(sims, fiftyPct);
        winRateStats.get(key).put(sims, winRate);
    }

    public Double getFiftyPct(String key, int sims) {
        return fiftyPctStats.get(key).get(sims);
    }

    public Double getWinRate(String key, int sims) {
        return winRateStats.get(key).get(sims);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Matchup,10k 50,10k avg,100k 50,100k avg,1000k 50,1000k avg\n");
        for (String key : fiftyPctStats.keySet()) {
            sb.append(key);
            for (int i = 10000; i <= 1000000; i *= 10) {
                sb.append(',');
                if (getFiftyPct(key, i) != null) sb.append(getFiftyPct(key, i));
                sb.append(',');
                if (getWinRate(key, i) != null) sb.append(getWinRate(key, i));
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
