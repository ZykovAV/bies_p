package ylab.bies.ideaservice.util.enums;

public enum Status {
    DRAFT(1),
    UNDER_CONSIDERATION(2),
    ACCEPTED(3),
    REJECTED(4);

    private final int value;

    Status(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
