package ru.practicum.explore.enums;

public enum EventState {
    PENDING,
    PUBLISHED,
    CANCELED;

    public static EventState from(String state) {
        for (EventState value : EventState.values()) {
            if (value.name().equalsIgnoreCase(state)) {
                return value;
            }
        }
        return null;
    }
}
