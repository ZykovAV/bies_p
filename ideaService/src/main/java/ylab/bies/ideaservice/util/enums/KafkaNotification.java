package ylab.bies.ideaservice.util.enums;

public enum KafkaNotification {

    PUBLISHED_MESSAGE("Idea published"),
    ACCEPTED_MESSAGE("Idea accepted"),
    REJECTED_MESSAGE("Idea rejected");

    private final String value;
    KafkaNotification(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
