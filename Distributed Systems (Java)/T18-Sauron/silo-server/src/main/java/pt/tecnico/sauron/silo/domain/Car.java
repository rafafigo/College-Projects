package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.exceptions.SauronException;

import static pt.tecnico.sauron.silo.domain.ObservedObject.Type.CAR;
import static pt.tecnico.sauron.silo.exceptions.ErrorMessage.INVALID_ID;
import static io.grpc.Status.INVALID_ARGUMENT;

public class Car extends ObservedObject {

    private final String id;

    public Car(String id) throws SauronException {
        super(CAR);
        checkId(id);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /***
     * Verifies Car id if with the correct format
     * @param id Car id
     * @throws SauronException When the Camera was not previously registered
     ***/
    @Override
    public void checkId(String id) throws SauronException {
        if (!id.matches("\\d{2}[A-Z]{4}" +
                "|\\d{4}[A-Z]{2}" +
                "|[A-Z]{2}\\d{4}" +
                "|[A-Z]{4}\\d{2}" +
                "|[A-Z]{2}\\d{2}[A-Z]{2}" +
                "|\\d{2}[A-Z]{2}\\d{2}")) {
            throw new SauronException(INVALID_ARGUMENT, INVALID_ID, id, CAR.toString());
        }
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Car && this.id.equals(((Car) obj).getId());
    }
}
