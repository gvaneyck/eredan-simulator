package eredan;

import lombok.EqualsAndHashCode;
import eredan.simulator.CharacterStatus;

import java.util.ArrayList;
import java.util.Collections;
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
            addAlly(myTeam.remove(0));
        }
        round = 0;
        phase = 0;
    }

    public void addAlly(CharacterStatus cs) {
        allies.add(cs);
        Collections.sort(allies, (cs1, cs2) -> Integer.compare(cs1.id, cs2.id));
    }

    public TeamState copy() {
        TeamState copy = new TeamState();
        copy.attacker = attacker;

        copy.allies = new ArrayList<>();
        for (CharacterStatus cs : allies) {
            copy.allies.add(cs.copy());
        }

        copy.enemies = new ArrayList<>();
        for (CharacterStatus cs : enemies) {
            copy.enemies.add(cs.copy());
        }

        copy.myWins = myWins;
        copy.theirWins = theirWins;
        copy.round = round;
        copy.attacker = attacker;
        copy.me = (me != null ? me.copy() : null);
        copy.them = (them != null ? them.copy() : null);
        copy.dice = dice;
        copy.phase = phase;

        return copy;
    }
}
