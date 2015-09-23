package eredan.simulator;

import eredan.dto.Ability;
import eredan.dto.Effect;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class BattleActionResolver {
    public static Logger log = Logger.getLogger("foo");
    static Map<String, TriConsumer<CharacterStatus, CharacterStatus, BattleArgs>> actions = new HashMap<>();

    static {
        actions.put("applyDamage", BattleActionResolver::applyDamage);
        actions.put("attack", BattleActionResolver::attack);
        actions.put("swordAttack", BattleActionResolver::swordAttack);
        actions.put("rage", BattleActionResolver::rage);
        actions.put("berserk", BattleActionResolver::berserk);
        actions.put("critical", BattleActionResolver::critical);
        actions.put("dodge", BattleActionResolver::dodge);
        actions.put("riposte", BattleActionResolver::riposte);
        actions.put("thorns", BattleActionResolver::thorns);
        actions.put("increaseStr", BattleActionResolver::increaseStr);
        actions.put("decreaseStr", BattleActionResolver::decreaseStr);
        actions.put("damageBuff", BattleActionResolver::damageBuff);
        actions.put("damageDebuff", BattleActionResolver::damageDebuff);
        actions.put("heal", BattleActionResolver::heal);
        actions.put("shield", BattleActionResolver::shield);
        actions.put("hit", BattleActionResolver::hit);
        actions.put("smite", BattleActionResolver::smite);
        actions.put("backstab", BattleActionResolver::backstab);
        actions.put("shock", BattleActionResolver::shock);
        actions.put("fireball", BattleActionResolver::fireball);
        actions.put("lightning", BattleActionResolver::lightning);
        actions.put("lifedrain", BattleActionResolver::lifedrain);
        actions.put("diceChangeRS", BattleActionResolver::diceChangeRS);
        actions.put("diceChangeBS", BattleActionResolver::diceChangeBS);
        actions.put("diceChangeYS", BattleActionResolver::diceChangeYS);
    }

    public static void execute(Ability ability, CharacterStatus source, CharacterStatus target) {
        int activations = countTriggers(source.diceCounts, ability.cost);
        for (int i = 0; i < activations; i++) {
            if (!hasDice(source.diceCounts, ability.cost)) {
                break;
            }

            for (Effect e : ability.effects) {
                int amount = getEffectAmount(e, source, target);
                execute(e.effect, amount, source, target);

                if (e.all) {
                    // TODO: All enemies
                    if (source.ally1 != null) {
                        amount = getEffectAmount(e, source.ally1, target);
                        execute(e.effect, amount, source.ally1, target);
                    }
                    if (source.ally2 != null) {
                        amount = getEffectAmount(e, source.ally2, target);
                        execute(e.effect, amount, source.ally2, target);
                    }
                }
            }
        }
    }

    public static int getEffectAmount(Effect e, CharacterStatus source, CharacterStatus target) {
        int amount = e.amount;

        if (e.icy) {
            amount += (int)((double)e.amount * target.ice / 10);
        }

        if ("sourceIsAttacker".equals(e.boostType) && source.isAttacker) {
            amount += e.boostAmount;
        } else if ("sourceIsDefender".equals(e.boostType) && !source.isAttacker) {
            amount += e.boostAmount;
        } else if ("sourceStr".equals(e.boostType)) {
            amount += source.str * e.boostAmount;
        } else if ("sourceRace".equals(e.boostType) && source.race.equals(e.boostCheck)) {
            amount += e.boostAmount;
        } else if ("sourceGuild".equals(e.boostType) && source.guild.equals(e.boostCheck)) {
            amount += e.boostAmount;
        } else if ("sourceClass".equals(e.boostType) && source.clazz.equals(e.boostCheck)) {
            amount += e.boostAmount;
        } else if ("targetRace".equals(e.boostType) && target.race.equals(e.boostCheck)) {
            amount += e.boostAmount;
        } else if ("targetGuild".equals(e.boostType) && target.guild.equals(e.boostCheck)) {
            amount += e.boostAmount;
        } else if ("targetClass".equals(e.boostType) && target.clazz.equals(e.boostCheck)) {
            amount += e.boostAmount;
        } else if ("alliesRace".equals(e.boostType)) {
            if (source.ally1 != null && e.boostCheck.equals(source.ally1.race)) {
                amount += e.boostAmount;
            }
            if (source.ally2 != null && e.boostCheck.equals(source.ally2.race)) {
                amount += e.boostAmount;
            }
        } else if ("alliesGuild".equals(e.boostType)) {
            if (source.ally1 != null && e.boostCheck.equals(source.ally1.guild)) {
                amount += e.boostAmount;
            }
            if (source.ally2 != null && e.boostCheck.equals(source.ally2.guild)) {
                amount += e.boostAmount;
            }
        } else if ("alliesClass".equals(e.boostType)) {
            if (source.ally1 != null && e.boostCheck.equals(source.ally1.clazz)) {
                amount += e.boostAmount;
            }
            if (source.ally2 != null && e.boostCheck.equals(source.ally2.clazz)) {
                amount += e.boostAmount;
            }
        } else if ("enemiesRace".equals(e.boostType)) {
            // TODO: Does this exist?
        } else if ("enemiesGuild".equals(e.boostType)) {
        } else if ("enemiesClass".equals(e.boostType)) {
        } else if ("diceS".equals(e.boostType)) {
            amount += source.diceCounts[0] * e.boostAmount;
        } else if ("diceR".equals(e.boostType)) {
            amount += source.diceCounts[1] * e.boostAmount;
        } else if ("diceB".equals(e.boostType)) {
            amount += source.diceCounts[2] * e.boostAmount;
        } else if ("diceY".equals(e.boostType)) {
            amount += source.diceCounts[3] * e.boostAmount;
        }

        return amount;
    }

    public static void execute(BattleAction effect, int amount, CharacterStatus source, CharacterStatus target) {
        BattleArgs args = new BattleArgs(amount);
        switch (effect) {
            case APPLY_DAMAGE:
                applyDamage(source, target, args);
                break;
            case ATTACK:
                attack(source, target, args);
                break;
            case SWORD_ATTACK:
                swordAttack(source, target, args);
                break;
            case RAGE:
                rage(source, target, args);
                break;
            case BERSERK:
                berserk(source, target, args);
                break;
            case CRITICAL:
                critical(source, target, args);
                break;
            case DODGE:
                dodge(source, target, args);
                break;
            case RIPOSTE:
                riposte(source, target, args);
                break;
            case THORNS:
                thorns(source, target, args);
                break;
            case INCREASE_STR:
                increaseStr(source, target, args);
                break;
            case DECREASE_STR:
                decreaseStr(source, target, args);
                break;
            case DAMAGE_BUFF:
                damageBuff(source, target, args);
                break;
            case DAMAGE_DEBUFF:
                damageDebuff(source, target, args);
                break;
            case HEAL:
                heal(source, target, args);
                break;
            case SHIELD:
                shield(source, target, args);
                break;
            case HIT:
                hit(source, target, args);
                break;
            case SMITE:
                smite(source, target, args);
                break;
            case BACKSTAB:
                backstab(source, target, args);
                break;
            case SHOCK:
                shock(source, target, args);
                break;
            case FIREBALL:
                fireball(source, target, args);
                break;
            case LIGHTNING:
                lightning(source, target, args);
                break;
            case LIFEDRAIN:
                lifedrain(source, target, args);
                break;
            case DICE_CHANGE_RS:
                diceChangeRS(source, target, args);
                break;
            case DICE_CHANGE_BS:
                diceChangeBS(source, target, args);
                break;
            case DICE_CHANGE_YS:
                diceChangeYS(source, target, args);
                break;
            case ICE:
                ice(source, target, args);
                break;
            default:
                throw new RuntimeException("Invalid action");
        }
    }

    public static int countTriggers(int[] diceCounts, int[] cost) {
        int triggers = 6;
        for (int i = 0; i < diceCounts.length; i++) {
            if (cost[i] != 0) {
                triggers = Math.min(triggers, diceCounts[i] / cost[i]);
            }
        }
        return triggers;
    }

    public static boolean hasDice(int[] diceCounts, int[] cost) {
        for (int i = 0; i < diceCounts.length; i++) {
            if (cost[i] != 0 && diceCounts[i] == 0) {
                return false;
            }
        }
        return true;
    }

    public static void adjustDamage(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        args.amount += source.damageBuff - source.damageDebuff;
        args.amount += target.defenseDebuff - target.defenseBuff;

        if (args.amount <= 0) {
            args.amount = 0;
        } else {
            if (source.crits > 0 && !args.isThorns) {
                source.crits--;
                args.amount *= 2;
            }
            if (target.dodge > 0) {
                target.dodge--;
                args.amount /= 2;
            }
        }
    }

    public static void applyDamage(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        if (target.shield > 0) {
            if (args.amount > target.shield) {
                args.amount -= target.shield;
                target.shield = 0;
            } else {
                target.shield -= args.amount;
                args.amount = 0;
            }
        }

        if (args.amount > 0) {
            target.damage += args.amount;
            if (!args.isThorns) {
                source.str += source.rage;
                target.str += target.berserk;
                if (target.thorns > 0) {
                    BattleArgs thornsArgs = new BattleArgs(target.thorns);
                    thornsArgs.isThorns = true;
                    applyDamage(target, source, thornsArgs);
                }
            }
        }
    }

    public static void attack(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        for (int i = 0; i < args.amount; i++) {
            BattleArgs hitArgs = new BattleArgs(source.str);
            adjustDamage(source, target, hitArgs);
            boolean triggerRiposte = (args.isSword && target.riposte > 0 && hitArgs.amount > target.shield);
            applyDamage(source, target, hitArgs);

            if (triggerRiposte) {
                target.riposte--;
                attack(target, source, new BattleArgs(1));
            }
        }
    }

    public static void swordAttack(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        args.isSword = true;
        attack(source, target, args);
    }

    public static void rage(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.rage += args.amount;
    }

    public static void berserk(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.berserk += args.amount;
    }

    public static void dodge(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.dodge += args.amount;
    }

    public static void critical(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.crits += args.amount;
    }

    public static void riposte(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.riposte += args.amount;
    }

    public static void thorns(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.thorns += args.amount;
    }

    public static void increaseStr(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.str += args.amount;
    }

    public static void decreaseStr(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        target.str = Math.max(0, target.str - args.amount);
    }

    public static void damageBuff(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.damageBuff += args.amount;
    }

    public static void damageDebuff(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        target.damageDebuff += args.amount;
    }

    public static void heal(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.damage = Math.max(0, source.damage - args.amount);
    }

    public static void shield(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.shield += args.amount;
    }

    public static void hit(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        adjustDamage(source, target, args);
        applyDamage(source, target, args);
    }

    public static void smite(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        BattleArgs smiteArgs = new BattleArgs(target.str * args.amount);
        adjustDamage(source, target, smiteArgs);
        applyDamage(source, target, smiteArgs);
    }

    public static void backstab(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        BattleArgs backstabArgs = new BattleArgs(args.amount / 2);
        decreaseStr(source, target, backstabArgs);
        adjustDamage(source, target, args);
        applyDamage(source, target, args);
    }

    public static void shock(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        applyDamage(source, target, args);
    }

    public static void fireball(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        target.damageDebuff += args.amount / 10;
        adjustDamage(source, target, args);
        applyDamage(source, target, args);
    }

    public static void lightning(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        int tempDebuff = args.amount / 10;
        adjustDamage(source, target, args);
        applyDamage(source, target, args);
        target.defenseDebuff += tempDebuff;
    }

    public static void lifedrain(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        heal(source, target, args);
        adjustDamage(source, target, args);
        applyDamage(source, target, args);
    }

    public static void diceChangeRS(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        int change = Math.min(args.amount, source.diceCounts[1]);
        source.diceCounts[1] -= change;
        source.diceCounts[0] += change;
    }

    public static void diceChangeBS(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        int change = Math.min(args.amount, source.diceCounts[2]);
        source.diceCounts[2] -= change;
        source.diceCounts[0] += change;
    }

    public static void diceChangeYS(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        int change = Math.min(args.amount, source.diceCounts[3]);
        source.diceCounts[3] -= change;
        source.diceCounts[0] += change;
    }

    public static void ice(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        target.ice += args.amount;
    }
}
