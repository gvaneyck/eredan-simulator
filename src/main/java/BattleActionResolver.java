import dto.Effect;

import java.util.HashMap;
import java.util.Map;

public class BattleActionResolver {
    static Map<String, TriConsumer<CharacterStatus, CharacterStatus, BattleArgs>> actions = new HashMap<>();

    static {
        actions.put("applyDamage", BattleActionResolver::applyDamage);
        actions.put("attack", BattleActionResolver::attack);
        actions.put("rage", BattleActionResolver::rage);
        actions.put("berserk", BattleActionResolver::berserk);
        actions.put("critical", BattleActionResolver::critical);
        actions.put("dodge", BattleActionResolver::critical);
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

    public static void execute(int[] cost, Effect e, CharacterStatus source, CharacterStatus target) {
        int amount = e.amount;

        // TODO:
        // - attacker/defender
        // - party buffs
        if ("sourceIsAttacker".equals(e.boostType)) {
        } else if ("sourceIsDefender".equals(e.boostType)) {
        } else if ("sourceStr".equals(e.boostType)) {
            amount += source.str * e.boostAmount;
        } else if ("targetRace".equals(e.boostType) && target.race.equals(e.boostCheck)) {
            amount += e.boostAmount;
        } else if ("targetGuild".equals(e.boostType) && target.guild.equals(e.boostCheck)) {
            amount += e.boostAmount;
        } else if ("targetClass".equals(e.boostType) && target.clazz.equals(e.boostCheck)) {
            amount += e.boostAmount;
        } else if ("alliesRace".equals(e.boostType) && source.race.equals(e.boostCheck)) {
            amount += e.boostAmount;
        } else if ("alliesGuild".equals(e.boostType) && source.guild.equals(e.boostCheck)) {
            amount += e.boostAmount;
        } else if ("alliesClass".equals(e.boostType) && source.clazz.equals(e.boostCheck)) {
            amount += e.boostAmount;
        } else if ("enemiesRace".equals(e.boostType) && target.race.equals(e.boostCheck)) {
            amount += e.boostAmount;
        } else if ("enemiesGuild".equals(e.boostType) && target.guild.equals(e.boostCheck)) {
            amount += e.boostAmount;
        } else if ("enemiesClass".equals(e.boostType) && target.clazz.equals(e.boostCheck)) {
            amount += e.boostAmount;
        } else if ("diceS".equals(e.boostType)) {
            amount += source.dice[0] * e.boostAmount;
        } else if ("diceR".equals(e.boostType)) {
            amount += source.dice[1] * e.boostAmount;
        } else if ("diceB".equals(e.boostType)) {
            amount += source.dice[2] * e.boostAmount;
        } else if ("diceY".equals(e.boostType)) {
            amount += source.dice[3] * e.boostAmount;
        }

        execute(cost, e.effect, amount, source, target);
    }

    public static void execute(int[] cost, String effect, int amount, CharacterStatus source, CharacterStatus target) {
        int triggers = triggers(source.diceCounts, cost);
        try {
            for (int i = 0; i < triggers; i++) {
                actions.get(effect).accept(source, target, new BattleArgs(amount));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int triggers(int[] diceCounts, int[] cost) {
        int triggers = 6;
        for (int i = 0; i < diceCounts.length; i++) {
            if (cost[i] != 0) {
                triggers = Math.min(triggers, diceCounts[i] / cost[i]);
            }
        }

        return triggers;
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
                    adjustDamage(target, source, thornsArgs);
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
        decreaseStr(source, target, args);
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
        target.defenseDebuff += args.amount / 10;
        adjustDamage(source, target, args);
        applyDamage(source, target, args);
    }

    public static void lifedrain(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        heal(source, target, args);
        adjustDamage(source, target, args);
        applyDamage(source, target, args);
    }

    public static void diceChangeRS(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        int change = Math.min(args.amount, source.dice[1]);
        source.diceCounts[1] -= change;
        source.diceCounts[0] += change;
    }

    public static void diceChangeBS(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        int change = Math.min(args.amount, source.dice[2]);
        source.diceCounts[2] -= change;
        source.diceCounts[0] += change;
    }

    public static void diceChangeYS(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        int change = Math.min(args.amount, source.dice[3]);
        source.diceCounts[3] -= change;
        source.diceCounts[0] += change;
    }
}
