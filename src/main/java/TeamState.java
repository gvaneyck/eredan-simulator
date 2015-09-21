import lombok.EqualsAndHashCode;
import simulator.CharacterStatus;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode
public class TeamState {
    public List<CharacterStatus> allies;
    public List<CharacterStatus> enemies;
    public int myWins;
    public int theirWins;
    public int round;
    public boolean attacker;
    public CharacterStatus me;
    public CharacterStatus them;
    public int dice;
    public int phase;

    public TeamState() { }

    public TeamState(List<CharacterStatus> myTeam) {
        allies = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            allies.add(myTeam.remove(0));
        }
        round = 0;
        phase = 0;
    }

    public TeamState copy() {
        TeamState copy = new TeamState();
        copy.attacker = attacker;

        copy.allies = new ArrayList<>();
        for (CharacterStatus cs : allies) {
            CharacterStatus csTemp = new CharacterStatus();
            csTemp.name = cs.name;
            csTemp.str = cs.str;
            copy.allies.add(csTemp);
        }

        copy.enemies = new ArrayList<>();
        for (CharacterStatus cs : enemies) {
            CharacterStatus csTemp = new CharacterStatus();
            csTemp.name = cs.name;
            csTemp.str = cs.str;
            copy.enemies.add(csTemp);
        }

        return copy;
    }
}
