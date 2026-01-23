package xyz.realplussmp.bounty.bounty;

public enum BountySortType {
    AMOUNT("Amount"),
    RECENT("Recently Set");

    private final String display;

    BountySortType(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }

    public BountySortType next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}