package simulator;

import dto.Ability;
import dto.Effect;
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

    public static void execute(String effect, int amount, CharacterStatus source, CharacterStatus target) {
        actions.get(effect).accept(source, target, new BattleArgs(amount));
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
                    log.trace(String.format("%s spiked %d", target.name, thornsArgs.amount));
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
            log.trace(String.format("%s attacked %d", source.name, hitArgs.amount));

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
        log.trace(String.format("%s rage %d", source.name, args.amount));
    }

    public static void berserk(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.berserk += args.amount;
        log.trace(String.format("%s berserk %d", source.name, args.amount));
    }

    public static void dodge(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.dodge += args.amount;
        log.trace(String.format("%s dodge %d", source.name, args.amount));
    }

    public static void critical(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.crits += args.amount;
        log.trace(String.format("%s crits %d", source.name, args.amount));
    }

    public static void riposte(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.riposte += args.amount;
        log.trace(String.format("%s riposte %d", source.name, args.amount));
    }

    public static void thorns(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.thorns += args.amount;
        log.trace(String.format("%s thorns %d", source.name, args.amount));
    }

    public static void increaseStr(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.str += args.amount;
        log.trace(String.format("%s increaseStr %d", source.name, args.amount));
    }

    public static void decreaseStr(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        target.str = Math.max(0, target.str - args.amount);
        log.trace(String.format("%s decreaseStr %d", source.name, args.amount));
    }

    public static void damageBuff(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.damageBuff += args.amount;
        log.trace(String.format("%s damageBuff %d", source.name, args.amount));
    }

    public static void damageDebuff(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        target.damageDebuff += args.amount;
        log.trace(String.format("%s damageDebuff %d", source.name, args.amount));
    }

    public static void heal(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.damage = Math.max(0, source.damage - args.amount);
        log.trace(String.format("%s heal %d", source.name, args.amount));
    }

    public static void shield(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.shield += args.amount;
        log.trace(String.format("%s shield %d", source.name, args.amount));
    }

    public static void hit(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        adjustDamage(source, target, args);
        applyDamage(source, target, args);
        log.trace(String.format("%s hit %d", source.name, args.amount));
    }

    public static void smite(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        BattleArgs smiteArgs = new BattleArgs(target.str * args.amount);
        adjustDamage(source, target, smiteArgs);
        applyDamage(source, target, smiteArgs);
        log.trace(String.format("%s smite %d", source.name, args.amount));
    }

    public static void backstab(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        BattleArgs backstabArgs = new BattleArgs(args.amount / 2);
        decreaseStr(source, target, backstabArgs);
        adjustDamage(source, target, args);
        applyDamage(source, target, args);
        log.trace(String.format("%s backstab %d", source.name, args.amount));
    }

    public static void shock(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        applyDamage(source, target, args);
        log.trace(String.format("%s shock %d", source.name, args.amount));
    }

    public static void fireball(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        target.damageDebuff += args.amount / 10;
        adjustDamage(source, target, args);
        applyDamage(source, target, args);
        log.trace(String.format("%s fireball %d", source.name, args.amount));
    }

    public static void lightning(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        int tempDebuff = args.amount / 10;
        adjustDamage(source, target, args);
        applyDamage(source, target, args);
        target.defenseDebuff += tempDebuff;
        log.trace(String.format("%s lightning %d", source.name, args.amount));
    }

    public static void lifedrain(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        heal(source, target, args);
        adjustDamage(source, target, args);
        applyDamage(source, target, args);
        log.trace(String.format("%s lifedrain %d", source.name, args.amount));
    }

    public static void diceChangeRS(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        int change = Math.min(args.amount, source.diceCounts[1]);
        source.diceCounts[1] -= change;
        source.diceCounts[0] += change;
        log.trace(String.format("%s R->S %d", source.name, change));
    }

    public static void diceChangeBS(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        int change = Math.min(args.amount, source.diceCounts[2]);
        source.diceCounts[2] -= change;
        source.diceCounts[0] += change;
        log.trace(String.format("%s B->S %d", source.name, change));
    }

    public static void diceChangeYS(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        int change = Math.min(args.amount, source.diceCounts[3]);
        source.diceCounts[3] -= change;
        source.diceCounts[0] += change;
        log.trace(String.format("%s Y->S %d", source.name, change));
    }
}
