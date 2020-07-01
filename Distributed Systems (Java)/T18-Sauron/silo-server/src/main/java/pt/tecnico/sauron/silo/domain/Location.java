package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.exceptions.SauronException;

import static pt.tecnico.sauron.silo.exceptions.ErrorMessage.INVALID_LATITUDE;
import static pt.tecnico.sauron.silo.exceptions.ErrorMessage.INVALID_LONGITUDE;
import static io.grpc.Status.INVALID_ARGUMENT;

public class Location {

    private final double latitude;
    private final double longitude;

    public Location(double latitude, double longitude) throws SauronException {
        checkCam_Location(latitude, longitude);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    /***
     * This Function Validates a Camera location
     * @param latitude Where the Camera is located
     * @param longitude Where the Camera is located
     * @throws SauronException When the Camera location is Invalid
     ***/
    public static void checkCam_Location(double latitude, double longitude) throws SauronException {

        if (latitude < -90 || latitude > 90) {
            throw new SauronException(INVALID_ARGUMENT, INVALID_LATITUDE, latitude, longitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new SauronException(INVALID_ARGUMENT, INVALID_LONGITUDE, latitude, longitude);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Location &&
                this.latitude == ((Location) obj).getLatitude() &&
                this.longitude == ((Location) obj).getLongitude();
    }
}
