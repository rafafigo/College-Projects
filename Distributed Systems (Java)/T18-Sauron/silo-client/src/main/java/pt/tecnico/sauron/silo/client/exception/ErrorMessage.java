package pt.tecnico.sauron.silo.client.exception;

public enum ErrorMessage {
    INVALID_PORT("Invalid Zoo Port: %s!"),
    INVALID_INSTANCE("Invalid Instance: %s!"),
    RECORDS_NOT_FOUND("Record(s) on path %s not found!");

    public final String label;

    ErrorMessage(String label) {
        this.label = label;
    }
}
