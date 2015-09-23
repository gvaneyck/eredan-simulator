package eredan.simulator;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape=JsonFormat.Shape.OBJECT)
public enum BattleAction {
    APPLY_DAMAGE,
    ATTACK,
    SWORD_ATTACK,
    RAGE,
    BERSERK,
    CRITICAL,
    DODGE,
    RIPOSTE,
    THORNS,
    INCREASE_STR,
    DECREASE_STR,
    DAMAGE_BUFF,
    DAMAGE_DEBUFF,
    HEAL,
    SHIELD,
    HIT,
    SMITE,
    BACKSTAB,
    SHOCK,
    FIREBALL,
    LIGHTNING,
    LIFEDRAIN,
    DICE_CHANGE_RS,
    DICE_CHANGE_BS,
    DICE_CHANGE_YS,
    ICE
    ;
}
