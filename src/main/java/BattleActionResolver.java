import java.util.HashMap;
import java.util.Map;

public class BattleActionResolver {
    static Map<String, TriConsumer<CharacterStatus, CharacterStatus, BattleArgs>> actions = new HashMap<>();

    static {
        actions.put("applyDamage", BattleActionResolver::applyDamage);
        actions.put("decreaseStr", BattleActionResolver::decreaseStr);
        actions.put("increaseStr", BattleActionResolver::increaseStr);
        actions.put("thorns", BattleActionResolver::thorns);
        actions.put("critical", BattleActionResolver::critical);
        actions.put("riposte", BattleActionResolver::riposte);
        actions.put("heal", BattleActionResolver::heal);
        actions.put("shield", BattleActionResolver::shield);
        actions.put("hit", BattleActionResolver::hit);
        actions.put("smite", BattleActionResolver::smite);
        actions.put("backstab", BattleActionResolver::backstab);
        actions.put("shock", BattleActionResolver::shock);
        actions.put("fireball", BattleActionResolver::fireball);
        actions.put("lightning", BattleActionResolver::lightning);
        actions.put("lifedrain", BattleActionResolver::lifedrain);
        actions.put("diceChange", BattleActionResolver::diceChange);
    }

    public static void execute(BattleAction battleAction, CharacterStatus source, CharacterStatus target) {
        actions.get(battleAction.action).accept(source, target, new BattleArgs(battleAction.amount));
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

    public static void decreaseStr(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        target.str = Math.max(0, target.str - args.amount);
    }

    public static void increaseStr(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.str += args.amount;
    }

    public static void thorns(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.thorns += args.amount;
    }

    public static void critical(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.crits += args.amount;
    }

    public static void riposte(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.riposte += args.amount;
    }

    public static void heal(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.damage = Math.max(0, source.damage - args.amount);
    }

    public static void shield(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        source.shield += args.amount;
    }

    public static void hit(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        for (int i = 0; i < args.amount; i++) {
            BattleArgs hitArgs = new BattleArgs(source.str);
            adjustDamage(source, target, hitArgs);
            boolean triggerRiposte = (args.isSword && target.riposte > 0 && hitArgs.amount > target.shield);
            applyDamage(source, target, hitArgs);

            if (triggerRiposte) {
                target.riposte--;
                hit(target, source, new BattleArgs(1));
            }
        }
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

    public static void diceChange(CharacterStatus source, CharacterStatus target, BattleArgs args) {
        // TODO
    }
}
