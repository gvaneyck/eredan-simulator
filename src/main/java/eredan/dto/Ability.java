package eredan.dto;

import java.util.List;

public class Ability {
    public int[] cost;
    public List<Effect> effects;

    public void setCost(String cost) {
        this.cost = new int[4];
        for (int i = 0; i < cost.length(); i++) {
            char c = cost.charAt(i);
            if (c == 'S') {
                this.cost[0]++;
            } else if (c == 'R') {
                this.cost[1]++;
            } else if (c == 'B') {
                this.cost[2]++;
            } else if (c == 'Y') {
                this.cost[3]++;
            }
        }
    }
}
