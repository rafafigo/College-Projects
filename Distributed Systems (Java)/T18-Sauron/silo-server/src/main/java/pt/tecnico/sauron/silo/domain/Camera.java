package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.exceptions.SauronException;

import static pt.tecnico.sauron.silo.exceptions.ErrorMessage.INVALID_CAMERA_NAME;
import static io.grpc.Status.INVALID_ARGUMENT;

public class Camera {

    private final String name;
    private final Location location;

    public Camera(String name, Location location) throws SauronException {
        checkCam_Name(name);
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    /***
     * Verifies if Camera name has the correct format
     * @param name of the Camera
     * @throws SauronException when the given Camera name is invalid
     ***/
    public static void checkCam_Name(String name) throws SauronException {

        if (!name.matches("[A-Za-z0-9]+") || name.length() < 3 || name.length() > 15) {
            throw new SauronException(INVALID_ARGUMENT, INVALID_CAMERA_NAME, name);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Camera && this.name.equals(((Camera) obj).getName());
    }
}
