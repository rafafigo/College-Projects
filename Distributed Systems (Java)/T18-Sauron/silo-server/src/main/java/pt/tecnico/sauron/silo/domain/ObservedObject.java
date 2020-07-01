package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.exceptions.SauronException;

public abstract class ObservedObject {

    public enum Type { CAR, PERSON }
    private final Type type;

    public ObservedObject(Type type) {
        this.type = type;
    }

    public Type getType(){
        return type;
    }

    public abstract void checkId(String id) throws SauronException;

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);
}
