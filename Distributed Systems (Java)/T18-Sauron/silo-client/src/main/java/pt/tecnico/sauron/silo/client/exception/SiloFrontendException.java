package pt.tecnico.sauron.silo.client.exception;

public class SiloFrontendException extends Exception {

    public SiloFrontendException(ErrorMessage errorMessage, String str) {
        super(String.format(errorMessage.label, str));
    }
}

