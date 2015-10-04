package eredan.simulator;

import eredan.dto.Ability;
import eredan.dto.Clazz;
import eredan.dto.Guild;
import eredan.dto.Hero;
import eredan.dto.Race;
import lombok.EqualsAndHashCode;

import java.util.List;

// Exclude elements only used during combat resolution, i.e. ones not important to the game state
@EqualsAndHashCode(exclude={"name", "guild", "race", "clazz", "diceId", "diceCounts", "isAttacker", "abilities", "ally1", "ally2",
                            "damage", "defenseDebuff", "bulwark", "scarabs", "stench"})
public class CharacterStatus implements Comparable<CharacterStatus> {
    public int id;
    public String name;
    public Guild guild;
    public Race race;
    public Clazz clazz;

    public int diceId;
    public int[] diceCounts;
    public boolean isAttacker;
    public List<Ability> abilities;
    public CharacterStatus[] allies = new CharacterStatus[2];

    public int str;
    public int damage = 0;
    public int shield = 0;
    public int damageBuff = 0;
    public int damageDebuff = 0;
    public int defenseBuff = 0;
    public int defenseDebuff = 0;
    public int terror = 0;
    public int rage = 0;
    public int berserk = 0;
    public int crits = 0;
    public int dodge = 0;
    public int thorns = 0;
    public int riposte = 0;
    public int blessing = 0;
    public int ice = 0;
    public int runes = 0;
    public int bulwark = 0;
    public int scarabs = 0;
    public int icyShield = 0;
    public int stench = 0;
    public int eclipse = 0;
    public int powder = 0;

    public CharacterStatus() { }

    public CharacterStatus(Hero hero) {
        this.id = hero.id;
        this.name = hero.name;
        this.guild = hero.guild;
        this.race = hero.race;
        this.clazz = hero.clazz;

        this.abilities = hero.abilities;

        this.str = hero.str;
    }

    public CharacterStatus copy() {
        CharacterStatus copy = new CharacterStatus();
        copy.id = id;
        copy.name = name;
        copy.str = str;

//        copy.damage = damage;
        copy.shield = shield;
        copy.damageBuff = damageBuff;
        copy.damageDebuff = damageDebuff;
        copy.defenseBuff = defenseBuff;
//        copy.defenseDebuff = defenseDebuff;
        copy.terror = terror;
        copy.rage = rage;
        copy.berserk = berserk;
        copy.crits = crits;
        copy.dodge = dodge;
        copy.thorns = thorns;
        copy.riposte = riposte;
        copy.blessing = blessing;
        copy.ice = ice;
        copy.runes = runes;
//        copy.bulwark = bulwark;
//        copy.scarabs = scarabs;
        copy.icyShield = icyShield;
//        copy.stench = stench;
//        copy.eclipse = eclipse;
        copy.powder = powder;

        return copy;
    }

    @Override
    public int compareTo(CharacterStatus o) {
        if (o == null) {
            return -1;
        } else {
            return Integer.compare(id, o.id);
        }
    }

    public String toString() {
        return "" + id;
    }
}
