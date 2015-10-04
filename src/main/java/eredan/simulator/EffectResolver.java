package eredan.simulator;

import eredan.dto.Ability;
import eredan.dto.Effect;
import eredan.dto.EffectType;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EffectResolver {
    public static Logger log = Logger.getLogger("foo");
    public static Random rand = new Random();

    public static void execute(Ability ability, CharacterStatus source, CharacterStatus target) {
        int activations = countTriggers(source.diceCounts, ability.cost);
        for (int i = 0; i < activations; i++) {
            if (!hasDice(source.diceCounts, ability.cost)) {
                break;
            }

            // Apply terror & stench
            source.damageDebuff += source.terror;
            target.str -= source.stench;

            for (Effect e : ability.effects) {
                int amount = getEffectAmount(e, source, target);
                execute(e.effect, amount, source, target);

                if (e.all) {
                    if (source.allies[0] != null) {
                        amount = getEffectAmount(e, source.allies[0], target);
                        execute(e.effect, amount, source.allies[0], target);
                    }
                    if (source.allies[1] != null) {
                        amount = getEffectAmount(e, source.allies[1], target);
                        execute(e.effect, amount, source.allies[1], target);
                    }
                }

                if (e.allOpponents) {
                    if (target.allies[0] != null) {
                        amount = getEffectAmount(e, source, target.allies[0]);
                        execute(e.effect, amount, source, target.allies[0]);
                    }
                    if (target.allies[1] != null) {
                        amount = getEffectAmount(e, source, target.allies[1]);
                        execute(e.effect, amount, source, target.allies[1]);
                    }
                }
            }
        }
    }

    public static int getEffectAmount(Effect e, CharacterStatus source, CharacterStatus target) {
        int amount = e.amount;

        if (e.boostType != null) {
            switch (e.boostType) {
                case IS_ATTACKER:
                    if (source.isAttacker) {
                        amount += e.boostAmount;
                    }
                    break;

                case IS_DEFENDER:
                    if (!source.isAttacker) {
                        amount += e.boostAmount;
                    }
                    break;

                case STR:
                    amount += source.str * e.boostAmount;
                    break;

                case OPP_STR:
                    amount += target.str * e.boostAmount;
                    break;

                case SOURCE_RACE:
                    if (source.race == e.raceCheck) {
                        amount += e.boostAmount;
                    }
                    break;

                case SOURCE_GUILD:
                    if (source.guild == e.guildCheck) {
                        amount += e.boostAmount;
                    }
                    break;

                case SOURCE_CLASS:
                    if (source.clazz == e.classCheck) {
                        amount += e.boostAmount;
                    }
                    break;

                case TARGET_RACE:
                    if (target.race == e.raceCheck) {
                        amount += e.boostAmount;
                    }
                    break;

                case TARGET_GUILD:
                    if (target.guild == e.guildCheck) {
                        amount += e.boostAmount;
                    }
                    break;

                case TARGET_CLASS:
                    if (target.clazz == e.classCheck) {
                        amount += e.boostAmount;
                    }
                    break;

                case ALLIES_RACE:
                    if (source.allies[0] != null && source.allies[0].race == e.raceCheck) {
                        amount += e.boostAmount;
                    }
                    if (source.allies[1] != null && source.allies[1].race == e.raceCheck) {
                        amount += e.boostAmount;
                    }
                    break;

                case ALLIES_GUILD:
                    if (source.allies[0] != null && source.allies[0].guild == e.guildCheck) {
                        amount += e.boostAmount;
                    }
                    if (source.allies[1] != null && source.allies[1].guild == e.guildCheck) {
                        amount += e.boostAmount;
                    }
                    break;

                case ALLIES_CLASS:
                    if (source.allies[0] != null && source.allies[0].clazz == e.classCheck) {
                        amount += e.boostAmount;
                    }
                    if (source.allies[1] != null && source.allies[1].clazz == e.classCheck) {
                        amount += e.boostAmount;
                    }
                    break;

                case DICE_S:
                    amount += source.diceCounts[Dice.SWORD] * e.boostAmount;
                    break;

                case DICE_R:
                    amount += source.diceCounts[Dice.RED] * e.boostAmount;
                    break;

                case DICE_B:
                    amount += source.diceCounts[Dice.BLUE] * e.boostAmount;
                    break;

                case DICE_Y:
                    amount += source.diceCounts[Dice.YELLOW] * e.boostAmount;
                    break;
            }
        }

        // These boosts happen after other boosts
        if (e.icy) {
            amount += e.amount * target.ice / 10;
        }

        if (e.runic) {
            amount += e.amount * source.runes / 2;
            source.runes = 0;
        }

        if (e.noble && source.damage > target.damage) {
            amount += e.amount / 2;
            source.runes = 0;
        }

        return amount;
    }

    public static void execute(EffectType effectType, int amount, CharacterStatus source, CharacterStatus target) {
        EffectArgs args = new EffectArgs(amount);
        switch (effectType) {
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
            case BUFF_STR:
                increaseStr(source, target, args);
                break;
            case DEBUFF_STR:
                decreaseStr(source, target, args);
                break;
            case BUFF_DMG:
                damageBuff(source, target, args);
                break;
            case DEBUFF_DMG:
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
            case TERROR:
                terror(source, target, args);
                break;
            case LIFEDRAIN:
                lifedrain(source, target, args);
                break;
            case BLESSING:
                blessing(source, target, args);
                break;
            case RESILIENCE:
                resilience(source, target, args);
                break;
            case SPELLBREAKER:
                spellbreaker(source, target, args);
                break;
            case DICE_CHANGE_RS:
                diceChange(Dice.RED, Dice.SWORD, source, target, args);
                break;
            case DICE_CHANGE_BS:
                diceChange(Dice.BLUE, Dice.SWORD, source, target, args);
                break;
            case DICE_CHANGE_YS:
                diceChange(Dice.YELLOW, Dice.SWORD, source, target, args);
                break;
            case DICE_CHANGE_BR:
                diceChange(Dice.BLUE, Dice.RED, source, target, args);
                break;
            case DICE_CHANGE_BY:
                diceChange(Dice.BLUE, Dice.YELLOW, source, target, args);
                break;
            case DICE_CHANGE_YR:
                diceChange(Dice.YELLOW, Dice.RED, source, target, args);
                break;
            case DICE_CHANGE_RB:
                diceChange(Dice.RED, Dice.BLUE, source, target, args);
                break;
            case DICE_CHANGE_YB:
                diceChange(Dice.YELLOW, Dice.BLUE, source, target, args);
                break;
            case ICE:
                ice(source, target, args);
                break;
            case RUNE:
                rune(source, target, args);
                break;
            case PURIFY:
                purify(source, target, args);
                break;
            case BULWARK:
                bulwark(source, target, args);
                break;
            case SHIELD_BASH:
                shieldBash(source, target, args);
                break;
            case SET_STR:
                setStr(source, target, args);
                break;
            case SCARAB:
                scarab(source, target, args);
                break;
            case DIVIDE_STR:
                divideStr(source, target, args);
                break;
            case BLIZZARD:
                blizzard(source, target, args);
                break;
            case STENCH:
                stench(source, target, args);
                break;
            case ECLIPSE:
                eclipse(source, target, args);
                break;
            case POWDER:
                powder(source, target, args);
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

    public static void adjustDamage(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        if (target.powder > 0 && args.isSword) {
            target.powder--;
            args.amount *= 3;
        }

        args.amount += source.damageBuff - source.damageDebuff;
        args.amount += target.defenseDebuff - target.defenseBuff;

        if (args.amount <= 0) {
            args.amount = 0;
        } else {
            if (source.crits > 0 && !args.isThorns) {
                source.crits--;
                args.amount *= 2;
            }

            if (target.icyShield > 0) {
                source.ice++;
                if (args.amount > target.icyShield) {
                    args.amount -= target.icyShield;
                    target.icyShield = 0;
                } else {
                    target.icyShield -= args.amount;
                    args.amount = 0;
                }
            }

            if (target.dodge > 0 && args.amount > 0) {
                target.dodge--;
                args.amount /= 2;
            }

            if (target.bulwark > 0 && args.amount > target.bulwark) {
                args.amount = target.bulwark;
            }
        }
    }

    public static void applyDamage(CharacterStatus source, CharacterStatus target, EffectArgs args) {
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
            if (target.scarabs > 0) {
                heal(target, source, args);
                target.scarabs--;
            } else {
                target.damage += args.amount;
                if (!args.isThorns) {
                    source.str += source.rage;
                    target.str += target.berserk;
                    if (target.thorns > 0) {
                        applyDamage(target, source, new EffectArgs(target.thorns, false, true));
                    }
                    if (source.blessing > 0) {
                        heal(source, target, new EffectArgs(source.blessing));
                    }
                }
            }
        }
    }

    public static void attack(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        for (int i = 0; i < args.amount; i++) {
            EffectArgs hitArgs = new EffectArgs(source.str, args.isSword, args.isThorns);
            adjustDamage(source, target, hitArgs);
            boolean triggerRiposte = (args.isSword && target.riposte > 0 && hitArgs.amount > target.shield);
            applyDamage(source, target, hitArgs);

            if (triggerRiposte) {
                target.riposte--;
                attack(target, source, new EffectArgs(1));
            }
        }
    }

    public static void swordAttack(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        args.isSword = true;
        attack(source, target, args);
    }

    public static void rage(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        source.rage += args.amount;
    }

    public static void berserk(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        source.berserk += args.amount;
    }

    public static void dodge(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        source.dodge += args.amount;
    }

    public static void critical(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        source.crits += args.amount;
    }

    public static void riposte(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        source.riposte += args.amount;
    }

    public static void thorns(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        source.thorns += args.amount;
    }

    public static void increaseStr(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        source.str += args.amount;
    }

    public static void decreaseStr(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        target.str = Math.max(0, target.str - args.amount);
    }

    public static void damageBuff(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        source.damageBuff += args.amount;
    }

    public static void damageDebuff(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        target.damageDebuff += args.amount;
    }

    public static void heal(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        if (source.eclipse > 0) {
            source.eclipse--;
        } else {
            source.damage = Math.max(0, source.damage - args.amount);
        }
    }

    public static void shield(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        source.shield += args.amount;
    }

    public static void hit(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        adjustDamage(source, target, args);
        applyDamage(source, target, args);
    }

    public static void smite(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        EffectArgs smiteArgs = new EffectArgs(target.str * args.amount);
        adjustDamage(source, target, smiteArgs);
        applyDamage(source, target, smiteArgs);
    }

    public static void backstab(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        decreaseStr(source, target, new EffectArgs(args.amount / 2));
        adjustDamage(source, target, args);
        applyDamage(source, target, args);
    }

    public static void shock(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        applyDamage(source, target, args);
    }

    public static void fireball(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        // TODO: Distinction between fireball/terror debuff/damage debuff for purify
        target.damageDebuff += args.amount / 10;
        adjustDamage(source, target, args);
        applyDamage(source, target, args);
    }

    public static void lightning(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        int tempDebuff = args.amount / 10;
        adjustDamage(source, target, args);
        applyDamage(source, target, args);
        target.defenseDebuff += tempDebuff;
    }

    public static void terror(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        // TODO: Distinction between fireball/terror debuff/damage debuff for purify
        target.terror += args.amount;
    }

    public static void lifedrain(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        heal(source, target, args);
        adjustDamage(source, target, args);
        applyDamage(source, target, args);
    }

    public static void resilience(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        source.defenseBuff += args.amount;
    }

    public static void blessing(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        source.blessing += args.amount;
    }

    public static void spellbreaker(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        adjustDamage(source, target, args);
        applyDamage(source, target, args);

        List<String> buffs = new ArrayList<>(10);
        if (target.shield > 0) {
            buffs.add("shield");
        } else if (target.damageBuff > 0) {
            buffs.add("damageBuff");
        } else if (target.defenseBuff > 0) {
            buffs.add("defenseBuff");
        } else if (target.rage > 0) {
            buffs.add("rage");
        } else if (target.berserk > 0) {
            buffs.add("berserk");
        } else if (target.crits > 0) {
            buffs.add("crits");
        } else if (target.dodge > 0) {
            buffs.add("dodge");
        } else if (target.thorns > 0) {
            buffs.add("thorns");
        } else if (target.riposte > 0) {
            buffs.add("riposte");
        } else if (target.runes > 0) {
            buffs.add("runes");
        } else if (target.bulwark > 0) {
            buffs.add("bulwark");
        } else if (target.scarabs > 0) {
            buffs.add("scarabs");
        } else if (target.icyShield > 0) {
            buffs.add("icyShield");
        } else if (target.stench > 0) {
            buffs.add("stench");
        }

        if (buffs.isEmpty()) {
            return;
        }

        String buffToRemove = buffs.get(rand.nextInt(buffs.size()));
        switch (buffToRemove) {
            case "shield":
                target.shield = 0;
                break;
            case "damageBuff":
                target.damageBuff = 0;
                break;
            case "defenseBuff":
                target.defenseBuff = 0;
                break;
            case "rage":
                target.rage = 0;
                break;
            case "berserk":
                target.berserk = 0;
                break;
            case "crits":
                target.crits = 0;
                break;
            case "dodge":
                target.dodge = 0;
                break;
            case "thorns":
                target.thorns = 0;
                break;
            case "riposte":
                target.riposte = 0;
                break;
            case "runes":
                target.runes = 0;
                break;
            case "bulwark":
                target.bulwark /= 2;
                break;
            case "scarabs":
                target.scarabs = 0;
                break;
            case "icyShield":
                target.icyShield = 0;
                break;
            case "stench":
                target.stench = 0;
                break;
        }
    }

    public static void diceChange(int sourceDice, int destDice, CharacterStatus source, CharacterStatus target, EffectArgs args) {
        // Make a copy of the dice since we're changing them
        if (source.diceCounts == Dice.counts[source.diceId]) {
            source.diceCounts = new int[4];
            System.arraycopy(Dice.counts[source.diceId], 0, source.diceCounts, 0, 4);
        }

        int change = Math.min(args.amount, source.diceCounts[sourceDice]);
        source.diceCounts[sourceDice] -= change;
        source.diceCounts[destDice] += change;
    }

    public static void ice(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        target.ice += args.amount;
    }

    public static void rune(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        source.runes += args.amount;
    }

    public static void purify(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        for (int i = 0; i < args.amount; i++) {
            source.damageDebuff /= 2;
            source.defenseDebuff /= 2;
            source.terror /= 2;
            source.ice /= 2;
            source.eclipse /= 2;
        }
    }

    public static void bulwark(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        source.bulwark = args.amount;
    }

    public static void shieldBash(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        EffectArgs bashDamage = new EffectArgs((int)(source.shield * 1.5 + args.amount));
        source.shield = 0;
        adjustDamage(source, target, bashDamage);
        applyDamage(source, target, bashDamage);
    }

    public static void setStr(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        // Special case: Ignore 0 (see Stone Eater)
        if (args.amount > 0) {
            source.str = args.amount;
        }
    }

    public static void scarab(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        source.scarabs += args.amount;
    }

    public static void divideStr(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        target.str /= args.amount;
    }

    public static void blizzard(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        source.icyShield += args.amount;
    }

    public static void stench(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        source.stench += args.amount;
    }

    public static void eclipse(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        target.eclipse += args.amount;
    }

    public static void powder(CharacterStatus source, CharacterStatus target, EffectArgs args) {
        target.powder += args.amount;
    }
}
