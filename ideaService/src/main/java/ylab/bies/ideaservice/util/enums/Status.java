package ylab.bies.ideaservice.util.enums;

/**
 * Idea's statuses
 */
public enum Status {
    DRAFT(1),
    UNDER_CONSIDERATION(2),
    ACCEPTED(3),
    REJECTED(4);

    private final Integer value;

    Status(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return this.value;
    }
}
