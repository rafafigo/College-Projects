package pt.tecnico.sauron.silo.exceptions;

public enum ErrorMessage {

    INVALID_REQUEST("Invalid %s: Not properly filled"),
    INVALID_CAMERA_NAME("Invalid Camera Name '%s'"),
    INVALID_LATITUDE("Invalid Latitude on Location (%f, %f)"),
    INVALID_LONGITUDE("Invalid Longitude on Location (%f, %f)"),
    INVALID_TYPE("Invalid type on Object: '%s'"),
    INVALID_ID("Invalid id '%s' on Object with type '%s'"),
    OUT_OF_BOUNDS_ID("id out of bounds '%s' on Object with type '%s'"),
    CAMERA_NAME_DUPLICATE("Camera '%s' already exists in Location (%f, %f)"),
    CAMERA_NAME_NOT_FOUND("Camera '%s' not found"),
    ID_NOT_FOUND("ID '%s' not found on Object with type"),
    TIMEOUT("The time to wait for Updates has been exceeded");

    public final String label;

    ErrorMessage(String label) {
        this.label = label;
    }
}
