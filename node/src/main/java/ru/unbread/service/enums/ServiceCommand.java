package ru.unbread.service.enums;

public enum ServiceCommand {
    HELP("/help"),
    CANCEL("/cancel"),
    START("/start"),
    DUTY("/duty"),
    CREATE_GROUP("/create"),
    JOIN("/join"),
    LIST_MEMBERS("/list_members"),
    LIST_ZONES("/list_zones"),
    ADD_ZONES("/add_zones"),
    UNKNOWN(null);

    private final String value;

    ServiceCommand(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static ServiceCommand fromValue(String value) {
        if (value == null || value.isBlank()) return UNKNOWN;

        for (ServiceCommand cmd : ServiceCommand.values()) {
            if (cmd.value != null && value.startsWith(cmd.value)) {
                return cmd;
            }
        }
        return UNKNOWN;
    }
}