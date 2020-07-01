package pt.tecnico.sauron.silo.domain.request;

import pt.tecnico.sauron.silo.Sauron;
import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.exceptions.SauronException;
import pt.tecnico.sauron.silo.replica.VecTimestamp;

public class CamJoinUpdate extends Update {

    private final Camera camera;

    public CamJoinUpdate(Integer ID, VecTimestamp prevTS, Camera camera) {
        super(Type.CAM_JOIN, ID, prevTS);
        this.camera = camera;
    }

    public Camera getCamera() {
        return camera;
    }

    @Override
    public void execute(Sauron sauron) throws SauronException {
        sauron.cam_join(camera);
    }
}
