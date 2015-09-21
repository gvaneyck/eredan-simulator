package simulator;

import dto.Ability;
import dto.Hero;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(exclude={"guild", "race", "clazz", "diceCounts", "isAttacker", "abilities", "ally1", "ally2"})
public class CharacterStatus implements Cloneable {
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
    public int rage = 0;
    public int berserk = 0;
    public int crits = 0;
    public int dodge = 0;
    public int thorns = 0;
    public int riposte = 0;

    public CharacterStatus() { }

    public CharacterStatus(Hero hero) {
        this.name = hero.name;
        this.guild = hero.guild;
        this.race = hero.race;
        this.clazz = hero.clazz;
        this.str = hero.str;

        this.abilities = hero.abilities;
    }
}
