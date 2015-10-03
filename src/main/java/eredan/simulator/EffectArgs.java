package eredan.simulator;

public class EffectArgs {
    int amount;
    boolean isSword = false;
    boolean isThorns = false;

    public EffectArgs(int amount) {
        this.amount = amount;
    }

    public EffectArgs(int amount, boolean isSword, boolean isThorns) {
        this.amount = amount;
        this.isSword = isSword;
        this.isThorns = isThorns;
    }
}
