package eredan.simulator;

import eredan.dto.Ability;
import eredan.dto.Hero;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(exclude={"name", "guild", "race", "clazz", "diceCounts", "isAttacker", "abilities", "ally1", "ally2"})
public class CharacterStatus implements Cloneable {
    public int id;
    public String name;
    public String guild;
    public String race;
    public String clazz;
    public int str;

    public int[] diceCounts;
    public boolean isAttacker;
    public List<Ability> abilities;
    public CharacterStatus ally1;
    public CharacterStatus ally2;

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

    public CharacterStatus() { }

    public CharacterStatus(Hero hero) {
        this.id = hero.id;
        this.name = hero.name;
        this.guild = hero.guild;
        this.race = hero.race;
        this.clazz = hero.clazz;
        this.str = hero.str;

        this.abilities = hero.abilities;
    }

    public CharacterStatus copy() {
        CharacterStatus copy = new CharacterStatus();
        copy.id = id;
        copy.name = name;
        copy.str = str;

        copy.damage = damage;
        copy.shield = shield;
        copy.damageBuff = damageBuff;
        copy.damageDebuff = damageDebuff;
        copy.defenseBuff = defenseBuff;
        copy.defenseDebuff = defenseDebuff;
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

        return copy;
    }
}
