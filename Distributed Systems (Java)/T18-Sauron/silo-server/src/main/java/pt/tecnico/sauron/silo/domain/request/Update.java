package pt.tecnico.sauron.silo.domain.request;

import pt.tecnico.sauron.silo.Sauron;
import pt.tecnico.sauron.silo.exceptions.SauronException;
import pt.tecnico.sauron.silo.replica.VecTimestamp;

public abstract class Update implements Comparable<Update> {
    public enum Type { CAM_JOIN, CAM_REPORT }

    private final Type type;
    private final Integer ID;
    private final VecTimestamp prevTS;

    public Update(Type type, Integer ID, VecTimestamp prevTS) {
        this.type = type;
        this.ID = ID;
        this.prevTS = prevTS;
    }

    public Type getType(){
        return this.type;
    }

    public Integer getID(){
        return this.ID;
    }

    public VecTimestamp getPrevTS() {
        return this.prevTS;
    }

    public Integer getSeq() {
        return this.prevTS.get(ID);
    }

    public abstract void execute(Sauron sauron) throws SauronException;

    @Override
    public int hashCode() {
        return this.ID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Update &&
                this.ID.equals(((Update) obj).getID()) &&
                this.getSeq().equals(((Update) obj).getSeq());
    }

    @Override
    public int compareTo(Update update) {
        return update.getPrevTS().isLess(this.prevTS, false) ? 1 : -1;
    }

    @Override
    public String toString() {
        String strType;
        switch (this.type) {
            case CAM_JOIN: strType = "camJoin"; break;
            case CAM_REPORT: strType = "report"; break;
            default: strType = "UNKNOWN";
        }
        return String.format("'%s' Update (ID: %d, TS: %s)", strType, this.ID, this.prevTS);
    }
}
