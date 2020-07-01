package pt.tecnico.sauron.silo.domain.request;

import pt.tecnico.sauron.silo.Sauron;
import pt.tecnico.sauron.silo.domain.ObservedObject;
import pt.tecnico.sauron.silo.exceptions.SauronException;
import pt.tecnico.sauron.silo.replica.VecTimestamp;

import java.util.List;

public class ReportUpdate extends Update {

    private final String camName;
    private final List<ObservedObject> objects;
    private final Long seconds;

    public ReportUpdate(Integer ID, VecTimestamp prevTS, String camName, List<ObservedObject> objects, Long seconds) {
        super(Type.CAM_REPORT, ID, prevTS);
        this.camName = camName;
        this.objects = objects;
        this.seconds = seconds;
    }

    public String getCamName() {
        return camName;
    }

    public List<ObservedObject> getObjects() {
        return objects;
    }

    public Long getSeconds() {
        return seconds;
    }

    @Override
    public void execute(Sauron sauron) throws SauronException {
        sauron.cam_report(this.camName, this.objects, this.seconds);
    }
}
