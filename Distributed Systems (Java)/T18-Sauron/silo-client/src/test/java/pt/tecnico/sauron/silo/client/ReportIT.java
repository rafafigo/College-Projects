package pt.tecnico.sauron.silo.client;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.client.exception.SiloFrontendException;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static io.grpc.Status.Code.INVALID_ARGUMENT;
import static io.grpc.Status.Code.NOT_FOUND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pt.tecnico.sauron.silo.grpc.Silo.Type.*;


public class ReportIT extends BaseIT {

    private static SiloFrontend frontend;
    private static final List<Camera> cameras = new ArrayList<>();
    private static final List<ObservedObject> persons = new ArrayList<>();
    private static final List<ObservedObject> cars = new ArrayList<>();

    @BeforeAll
    public static void oneTimeSetUp() {
        /* Connects to Server */
        final String zooHost = BaseIT.testProps.getProperty("zoo.host");
        final String zooPort = BaseIT.testProps.getProperty("zoo.port");
        final Integer timeout = Integer.parseInt(BaseIT.testProps.getProperty("timeout"));
        final Integer retries = Integer.parseInt(BaseIT.testProps.getProperty("retries"));
        try {
            frontend = new SiloFrontend(zooHost, zooPort, timeout, retries);
        } catch (SiloFrontendException e) {
            System.err.println(e.getMessage());
        }
        frontend.ctrl_clear(CtrlClearRequest.getDefaultInstance());

        Location location = Location.newBuilder()
                .setLongitude(20)
                .setLatitude(20)
                .build();

        for (int i = 0; i < 3; i++) {
            cameras.add(Camera.newBuilder()
                    .setName("Camera" + i)
                    .setLocation(location)
                    .build());
            persons.add(ObservedObject.newBuilder()
                    .setType(PERSON)
                    .setId("12345" + i)
                    .build());
            cars.add(ObservedObject.newBuilder()
                    .setId("ABCD0"+ i)
                    .setType(CAR)
                    .build());
        }
    }

    @AfterAll
    public static void oneTimeTearDown() {
        /* Disconnects with server */
        frontend.shutdown();
    }

    @BeforeEach
    public void setUp() {
        /* Sets Up the Server */
        frontend.ctrl_init(CtrlInitRequest.newBuilder().addAllCameras(cameras).build());
    }

    @AfterEach
    public void tearDown() {
        /* Clears all the information in the Server */
        frontend.ctrl_clear(CtrlClearRequest.getDefaultInstance());
    }

    /*Tests*/

    @Test
    /* Checks if Report of Type PERSON is added to Server */
    public void checkReport_PERSON(){
        frontend.report(ReportRequest.newBuilder()
                .setCamName(cameras.get(0).getName())
                .addObjects(persons.get(0))
                .build());

        CtrlPingResponse response = frontend.ctrl_ping(CtrlPingRequest.getDefaultInstance());
        Observation observation = response.getObservations(0);

        Instant instant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Timestamp ts = Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .build();

        assertTrue(observation.getCamera().equals(cameras.get(0)) &&
                observation.getObject().equals(persons.get(0)) &&
                observation.getTs().getSeconds() <= ts.getSeconds());
    }

    @Test
    /* Checks if Report of Type CAR is added to Server */
    public void checkReport_CAR(){
        frontend.report(ReportRequest.newBuilder()
                .setCamName(cameras.get(0).getName())
                .addObjects(cars.get(0))
                .build());

        CtrlPingResponse response = frontend.ctrl_ping(CtrlPingRequest.getDefaultInstance());
        Observation observation = response.getObservations(0);

        Instant instant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Timestamp ts = Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .build();

        assertTrue(observation.getCamera().equals(cameras.get(0)) &&
                observation.getObject().equals(cars.get(0)) &&
                observation.getTs().getSeconds() <= ts.getSeconds());
    }

    @Test
    /* Checks if Report fails with non existing Camera */
    public void checkReportWrongCamName() {
        assertEquals(NOT_FOUND, assertThrows(StatusRuntimeException.class, () -> {
            frontend.report(ReportRequest.newBuilder()
                    .setCamName("Camera3")
                    .addAllObjects(cars)
                    .build());
        }).getStatus().getCode());
    }

    @Test
    /* Checks if Report fails with wrong ID of Type PERSON */
    public void checkReportWrongId_PERSON() {
        ObservedObject person = ObservedObject.newBuilder()
                .setId("12A3")
                .setType(PERSON)
                .build();

        assertEquals(INVALID_ARGUMENT, assertThrows(StatusRuntimeException.class, () -> {
            frontend.report(ReportRequest.newBuilder()
                    .setCamName(cameras.get(0).getName())
                    .addObjects(person)
                    .build());
        }).getStatus().getCode());
    }

    @Test
    /* Checks if Report fails with wrong ID of Type CAR */
    public void checkReportWrongId_CAR() {
        ObservedObject car = ObservedObject.newBuilder()
                .setId("123ABC")
                .setType(CAR)
                .build();

        assertEquals(INVALID_ARGUMENT, assertThrows(StatusRuntimeException.class, () -> {
            frontend.report(ReportRequest.newBuilder()
                    .setCamName(cameras.get(0).getName())
                    .addObjects(car)
                    .build());
        }).getStatus().getCode());
    }

    @Test
    /* Checks if Report fails with wrong Type */
    public void checkReportWrongType() {
        ObservedObject object = ObservedObject.newBuilder()
                .setId("ABCD12")
                .setType(UNKNOWN)
                .build();

        assertEquals(INVALID_ARGUMENT, assertThrows(StatusRuntimeException.class, () -> {
            frontend.report(ReportRequest.newBuilder()
                    .setCamName(cameras.get(0).getName())
                    .addObjects(object)
                    .build());
        }).getStatus().getCode());
    }

    @Test
    /* Checks if Report of multiple objects list is added to Server */
    public void checkReportMultipleObjects() {
        List<ObservedObject> objects = new ArrayList<>();
        objects.addAll(persons);
        objects.addAll(cars);

        frontend.report(ReportRequest.newBuilder()
                .setCamName(cameras.get(0).getName())
                .addAllObjects(objects)
                .build());

        CtrlPingResponse response = frontend.ctrl_ping(CtrlPingRequest.getDefaultInstance());
        List<Observation> observations = response.getObservationsList();

        assertEquals(persons.size() + cars.size(), observations.size());
    }

    @Test
    /* Checks if Report of empty objects list doesn't fail */
    public void checkReportEmptyObjectList() {
        List<ObservedObject> objects = new ArrayList<>();

        frontend.report(ReportRequest.newBuilder()
                .setCamName(cameras.get(0).getName())
                .addAllObjects(objects)
                .build());

        CtrlPingResponse response = frontend.ctrl_ping(CtrlPingRequest.getDefaultInstance());
        List<Observation> observations = response.getObservationsList();

        assertEquals(0, observations.size());
    }
}
