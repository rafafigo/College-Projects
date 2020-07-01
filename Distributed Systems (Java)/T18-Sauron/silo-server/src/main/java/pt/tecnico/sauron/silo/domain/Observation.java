package pt.tecnico.sauron.silo.domain;


public class Observation implements Comparable<Observation> {

    private final ObservedObject observedObject;
    private final Camera camera;
    private final Long seconds;

    public Observation(ObservedObject observedObject, Camera camera, Long seconds) {
        this.observedObject = observedObject;
        this.camera = camera;
        this.seconds = seconds;
    }

    public ObservedObject getObservedObject() {
        return observedObject;
    }

    public Camera getCamera() {
        return camera;
    }

    public Long getSeconds() {
        return seconds;
    }

    /***
     * Compares the seconds of two Observations
     * @param other Observation to be compared
     * @return int result of the comparison
     ***/
    @Override
    public int compareTo(Observation other) {
        if (this.seconds.equals(other.seconds)) return 0;
        return this.seconds < other.seconds ? 1 : -1;
    }
}
