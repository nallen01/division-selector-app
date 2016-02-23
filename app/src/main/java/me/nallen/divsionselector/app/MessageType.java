package me.nallen.divsionselector.app;

public enum MessageType {
    CLEAR_ALL(0),
    CLEAR_TEAMS(1),
    CLEAR_DIVISIONS(2),

    ADD_TEAM(3),
    ASSIGN_TEAM(4),
    UNASSIGN_TEAM(5),
    RANDOMISE_TEAMS(8),

    ADD_DIVISION(6),
    REMOVE_DIVISION(7);

    private final int id;
    MessageType(int id) { this.id = id; }
    public int getValue() { return id; }
    public static MessageType fromInt(int id) {
        MessageType[] values = MessageType.values();
        for(int i=0; i<values.length; i++) {
            if(values[i].getValue() == id)
                return values[i];
        }
        return null;
    }
}
