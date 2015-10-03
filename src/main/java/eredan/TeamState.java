package eredan;

import eredan.simulator.CharacterStatus;
import lombok.EqualsAndHashCode;

import java.util.Arrays;

@EqualsAndHashCode
public class TeamState {
    public CharacterStatus[] allies;
    public CharacterStatus[] enemies;
    public int myWins;
    public int theirWins;
    public int round;
    public boolean attacker;
    public CharacterStatus me;
    public CharacterStatus them;
    public int dice;
    public int phase;

    public TeamState() { }

    public TeamState(CharacterStatus[] myTeam) {
        allies = new CharacterStatus[3];
        allies[0] = myTeam[0];
        allies[1] = myTeam[1];
        allies[2] = myTeam[2];
        Arrays.sort(allies);
    }

    public void replaceAlly(int pos, CharacterStatus cs) {
        allies[pos] = cs;
        Arrays.sort(allies);
    }

    public void sortAllies() {
        // Handles null "removed" allies
        if (allies[0] == null) {
            allies[0] = allies[1];
            allies[1] = allies[2];
            allies[2] = null;
        } else if (allies[1] == null) {
            allies[1] = allies[2];
            allies[2] = null;
        }
    }

    public TeamState copy() {
        TeamState copy = new TeamState();

        copy.allies = new CharacterStatus[3];
        for (int i = 0; i < 3; i++) {
            if (allies[i] != null) {
                copy.allies[i] = allies[i].copy();
            }
        }

        copy.enemies = new CharacterStatus[3];
        for (int i = 0; i < 3; i++) {
            if (enemies[i] != null) {
                copy.enemies[i] = enemies[i].copy();
            }
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

    public String toString() {
        return String.format("%s %s %d %d %d %d %d %d %d %d",
                Arrays.toString(allies),
                Arrays.toString(enemies),
                myWins,
                theirWins,
                round,
                attacker ? 1 : 0,
                me != null ? me.id : -1,
                them != null ? them.id : -1,
                dice,
                phase
                );
    }
}
