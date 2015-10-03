package eredan.simulator;

import eredan.dto.Ability;
import eredan.dto.Effect;
import eredan.dto.EffectType;
import eredan.dto.Hero;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BattleSimulator {
    public static final int TIE = 0;
    public static final int P1_WIN = 1;
    public static final int P2_WIN = 2;

    public static Map<String, Integer[][]> battleResults = new HashMap<>();

    public static Ability basicAttack = new Ability();
    static {
        Effect e = new Effect(EffectType.SWORD_ATTACK, 1);
        List<Effect> effects = new ArrayList<>(1);
        effects.add(e);

        basicAttack.effects = effects;
        basicAttack.setCost("S");
    }

    public static int getResults(CharacterStatus p1, CharacterStatus p2, int dice1, int dice2) {
        String key = p1.toString() + '|' + p2.toString();
        if (battleResults.containsKey(key)) {
            battleResults.put(key, simulateFull(p1, p2));
        }
        return battleResults.get(key)[dice1][dice2];
    }

    public static Integer[][] simulateFull(CharacterStatus p1, CharacterStatus p2) {
        Integer[][] results = new Integer[84][84];
        for (int i = 0; i < 84; i++) {
            for (int j = 0; j < 84; j++) {
                results[i][j] = simulate(p1, p2, i, j);
            }
        }

        return results;
    }

    public static int simulate(int p1, int p2, int p1dice, int p2dice) {
        return simulate(new CharacterStatus(Heroes.heroes.get(p1)), new CharacterStatus(Heroes.heroes.get(p2)), p1dice, p2dice);
    }

    public static int simulate(Hero p1, Hero p2, int p1dice, int p2dice) {
        return simulate(new CharacterStatus(p1), new CharacterStatus(p2), p1dice, p2dice);
    }

    public static int simulate(CharacterStatus p1, CharacterStatus p2, int p1dice, int p2dice) {
        p1.isAttacker = true;
        p1.diceCounts = Dice.counts[p1dice];
        p2.diceCounts = Dice.counts[p2dice];

        // Abilities
        for (int round = 0; round < 3; round++) {
            EffectResolver.execute(p1.abilities.get(round), p1, p2);
            EffectResolver.execute(p2.abilities.get(round), p2, p1);
        }

        // Runes
        // TODO: Are these individual hits?
        for (int i = 0; i < p1.runes; i++) {
            EffectResolver.execute(EffectType.HIT, 200, p1, p2);
        }
        for (int i = 0; i < p2.runes; i++) {
            EffectResolver.execute(EffectType.HIT, 200, p2, p1);
        }

        // Swords
        EffectResolver.execute(basicAttack, p1, p2);
        EffectResolver.execute(basicAttack, p2, p1);

        if (p1.damage < p2.damage) {
            return P1_WIN;
        } else if (p1.damage > p2.damage) {
            return P2_WIN;
        } else {
            return TIE;
        }
    }
}
