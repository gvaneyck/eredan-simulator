package dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Effect {
    public String effect;
    public int amount;
    public boolean all;
    @JsonProperty("boost_type") public String boostType;
    @JsonProperty("boost_check") public String boostCheck;
    @JsonProperty("boost_amount") public int boostAmount;

    public Effect() { }

    public Effect(String effect, int amount) {
        this.effect = effect;
        this.amount = amount;
    }
}
