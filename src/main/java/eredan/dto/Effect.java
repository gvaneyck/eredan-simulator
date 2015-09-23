package eredan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import eredan.simulator.BattleAction;

public class Effect {
    public BattleAction effect;
    public int amount;
    public boolean all;
    public boolean icy;
    @JsonProperty("boost_type") public String boostType;
    @JsonProperty("boost_check") public String boostCheck;
    @JsonProperty("boost_amount") public int boostAmount;

    public Effect() { }

    public Effect(BattleAction effect, int amount) {
        this.effect = effect;
        this.amount = amount;
    }
}
