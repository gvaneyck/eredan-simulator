package eredan.simulator;

public class BattleArgs {
    int amount;
    boolean isSword = false;
    boolean isThorns = false;

    public BattleArgs(int amount) {
        this.amount = amount;
    }

    public BattleArgs(int amount, boolean isSword, boolean isThorns) {
        this.amount = amount;
        this.isSword = isSword;
        this.isThorns = isThorns;
    }
}
