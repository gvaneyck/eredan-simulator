package eredan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Effect {
    public EffectType effect;
    public int amount;

    public boolean icy;
    public boolean runic;
    public boolean noble;

    public boolean all;
    @JsonProperty("all_opp") public boolean allOpponents;

    @JsonProperty("boost_type") public BoostType boostType;
    @JsonProperty("race_check") public Race raceCheck;
    @JsonProperty("guild_check") public Guild guildCheck;
    @JsonProperty("class_check") public Clazz classCheck;
    @JsonProperty("boost_amount") public int boostAmount;

    public Effect() { }

    public Effect(EffectType effect, int amount) {
        this.effect = effect;
        this.amount = amount;
    }
}
