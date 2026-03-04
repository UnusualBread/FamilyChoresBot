package ru.unbread.service.enums;

public enum ServiceCommand {
    HELP("/help"),
    REGISTRATION("/registration"),
    CANCEL("/cancel"),
    START("/start");
    private final String value;

    ServiceCommand(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static ServiceCommand fromValue(String value) {
        for (ServiceCommand cmd : ServiceCommand.values()) {
            if (cmd.value.equals(value)) {
                return cmd;
            }
        }
        return null;
    }
}
