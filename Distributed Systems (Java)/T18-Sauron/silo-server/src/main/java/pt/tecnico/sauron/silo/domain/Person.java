package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.exceptions.SauronException;
import static pt.tecnico.sauron.silo.domain.ObservedObject.Type.PERSON;

import static pt.tecnico.sauron.silo.exceptions.ErrorMessage.INVALID_ID;
import static pt.tecnico.sauron.silo.exceptions.ErrorMessage.OUT_OF_BOUNDS_ID;
import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.OUT_OF_RANGE;

public class Person extends ObservedObject {

    private final Long id;

    public Person(String id) throws SauronException {
        super(PERSON);
        checkId(id);
        this.id = Long.parseLong(id);
    }

    public Long getId() {
        return id;
    }

    /***
     * Verifies Person id if with the correct format
     * @param id Person id
     * @throws SauronException when id is invalid
     ***/
    @Override
    public void checkId(String id) throws SauronException {
        if (!id.matches("\\d+")) {
            throw new SauronException(INVALID_ARGUMENT, INVALID_ID, id, PERSON.toString());
        }
        try { Long.parseLong(id); }
        catch (NumberFormatException e) {
            throw new SauronException(OUT_OF_RANGE, OUT_OF_BOUNDS_ID, id, PERSON.toString());
        }
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Person && this.id.equals(((Person) obj).getId());
    }
}
