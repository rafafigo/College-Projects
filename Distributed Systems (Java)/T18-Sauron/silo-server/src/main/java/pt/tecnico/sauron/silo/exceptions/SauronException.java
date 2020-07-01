package pt.tecnico.sauron.silo.exceptions;

import io.grpc.Status;

public class SauronException extends Exception {

    private final Status status;

    public SauronException(Status status, ErrorMessage errorMessage) {
        this.status = status.withDescription(errorMessage.label);
    }

    public SauronException(Status status, ErrorMessage errorMessage, String str) {
        this.status = status.withDescription(String.format(errorMessage.label, str));
    }

    public SauronException(Status status, ErrorMessage errorMessage, double d1, double d2) {
        this.status = status.withDescription(String.format(errorMessage.label, d1, d2));
    }

    public SauronException(Status status, ErrorMessage errorMessage, String str, double d1, double d2) {
        this.status = status.withDescription(String.format(errorMessage.label, str, d1, d2));
    }

    public SauronException(Status status, ErrorMessage errorMessage, String str1, String str2) {
        this.status = status.withDescription(String.format(errorMessage.label, str1, str2));
    }

    public Status getStatus() {
        return status;
    }
}
