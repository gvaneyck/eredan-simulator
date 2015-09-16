public class CharacterStatus {
    public int str;
    public String guild;
    public String race;
    public String clazz;

    public int[] dice;

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

    public CharacterStatus(int str, String guild, String race, String clazz) {
        this.str = str;
        this.guild = guild;
        this.race = race;
        this.clazz = clazz;
    }
}
