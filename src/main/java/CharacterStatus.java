import dto.Hero;

public class CharacterStatus {
    public String name;
    public String guild;
    public String race;
    public String clazz;
    public int str;

    public int[] dice;
    public int[] diceCounts;
    public boolean isAttacker;

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

    public CharacterStatus(Hero hero, int[] dice) {
        this.name = hero.name;
        this.guild = hero.guild;
        this.race = hero.race;
        this.clazz = hero.clazz;
        this.str = hero.str;

        this.dice = dice;
        this.diceCounts = new int[4];
        for (int i = 0; i < dice.length; i++) {
            this.diceCounts[dice[i]]++;
        }
    }
}
