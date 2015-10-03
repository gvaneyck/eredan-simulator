package eredan.dto;

import eredan.simulator.Clazz;
import eredan.simulator.Guild;
import eredan.simulator.Race;

import java.util.List;

public class Hero {
    public int id;
    public String name;
    public Guild guild;
    public Race race;
    public Clazz clazz;
    public int str;
    public List<Ability> abilities;
}
