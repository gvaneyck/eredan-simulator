package eredan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import eredan.simulator.BattleAction;
import eredan.simulator.BoostType;

public class Effect {
    public BattleAction effect;
    public int amount;

    public boolean icy;
    public boolean runic;

    public boolean all;
    @JsonProperty("all_opp") public boolean allOpponents;

    @JsonProperty("boost_type") public BoostType boostType;
    @JsonProperty("boost_check") public String boostCheck;
    @JsonProperty("boost_amount") public int boostAmount;

    public Effect() { }

    public Effect(BattleAction effect, int amount) {
        this.effect = effect;
        this.amount = amount;
    }
}
