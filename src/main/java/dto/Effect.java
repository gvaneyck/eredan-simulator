package dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Effect {
    public String effect;
    public int amount;
    @JsonProperty("boost_type") public String boostType;
    @JsonProperty("boost_amount") public int boostAmount;
    @JsonProperty("dice_lost") public int diceLost;
    @JsonProperty("dice_gained") public int diceGained;
}
