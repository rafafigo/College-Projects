package pt.tecnico.sauron.silo.client;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.client.exception.SiloFrontendException;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.util.ArrayList;
import java.util.List;

import static io.grpc.Status.Code.INVALID_ARGUMENT;
import static io.grpc.Status.Code.NOT_FOUND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class TrackTraceIT extends BaseIT {

	private static SiloFrontend frontend;
	private static final List<ObservedObject> persons = new ArrayList<>();
	private static final List<ObservedObject> cars = new ArrayList<>();
	private static final List<Timestamp> timestamps = new ArrayList<>();
	private static final List<Observation> observations = new ArrayList<>();

	@BeforeAll
	public static void oneTimeSetUp() {
		/* Connect to Server */
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

		/* Create a Camera */
		Location location = Location.newBuilder()
				.setLatitude(30)
				.setLongitude(30)
				.build();
		Camera camera = Camera.newBuilder()
				.setName("Camera1337")
				.setLocation(location)
				.build();
		/* Create Observations */
		for (int i = 0; i < 2; i++) {
			persons.add(ObservedObject.newBuilder()
					.setType(Type.PERSON)
					.setId("" + i)
					.build());
			cars.add(ObservedObject.newBuilder()
					.setType(Type.CAR)
					.setId("AA000" + i)
					.build());
			timestamps.add(Timestamp.newBuilder()
					.setSeconds(500 - i)
					.build());
		}
		for (Timestamp ts : timestamps) {
			for (ObservedObject person : persons) {
				observations.add(Observation.newBuilder()
						.setCamera(camera)
						.setObject(person)
						.setTs(ts)
						.build());
			}
			for (ObservedObject car : cars) {
				observations.add(Observation.newBuilder()
						.setCamera(camera)
						.setObject(car)
						.setTs(ts)
						.build());
			}
		}
		/* SetUp Server */
		frontend.ctrl_init(CtrlInitRequest.newBuilder()
				.addCameras(camera)
				.addAllObservations(observations)
				.build()
		);
	}

	@AfterAll
	public static void oneTimeTearDown() {
		/* TearDown Server */
		frontend.ctrl_clear(CtrlClearRequest.getDefaultInstance());
		/* Disconnect to Server */
		frontend.shutdown();
	}

	@BeforeEach
	public void setUp() {
	}
	
	@AfterEach
	public void tearDown() {
	}

	/* Track Tests */
	@Test
	/* Check PERSON Track Observation & Observation Object */
	public void checkTrack_PERSON() {
		Observation obs = frontend.track(TrackRequest.newBuilder()
				.setObject(persons.get(0))
				.build())
				.getObservation();
		assertEquals(persons.get(0), obs.getObject());
	}

	@Test
	/* Check CAR Track Observation & Observation Object */
	public void checkTrack_CAR() {
		Observation obs = frontend.track(TrackRequest.newBuilder()
				.setObject(cars.get(0))
				.build())
				.getObservation();
		assertEquals(cars.get(0), obs.getObject());
	}

	@Test
	/* Check PERSON Track Observation & Most Recent Observation */
	public void checkTrackTimestamp_PERSON() {
		Observation obs = frontend.track(TrackRequest.newBuilder()
				.setObject(persons.get(0))
				.build())
				.getObservation();
		assertEquals(timestamps.get(0), obs.getTs());
	}

	@Test
	/* Check CAR Track Observation & Most Recent Observation */
	public void checkTrackTimestamp_CAR() {
		Observation obs = frontend.track(TrackRequest.newBuilder()
				.setObject(cars.get(0))
				.build())
				.getObservation();
		assertEquals(timestamps.get(0), obs.getTs());
	}

	@Test
	/* Check NOTFOUND Exception | Try to Track a Non Observed Object */
	public void checkTrackEmpty_NOTFOUND() {
		ObservedObject obj = ObservedObject.newBuilder()
				.setType(Type.PERSON)
				.setId("3")
				.build();
		assertEquals(NOT_FOUND, assertThrows(StatusRuntimeException.class, () -> {
			frontend.track(TrackRequest.newBuilder()
					.setObject(obj)
					.build());
		}).getStatus().getCode());
	}

	@Test
	/* Check INVALID_ARGUMENT Exception | Try to Track an Invalid Observed Object */
	public void checkTrackEmpty_INVALID() {
		ObservedObject obj = ObservedObject.newBuilder()
				.setType(Type.CAR)
				.setId("3")
				.build();
		assertEquals(INVALID_ARGUMENT, assertThrows(StatusRuntimeException.class, () -> {
			frontend.track(TrackRequest.newBuilder()
					.setObject(obj)
					.build());
		}).getStatus().getCode());
	}

	/* TrackMatch Tests */
	@Test
	/* Check PERSON TrackMatch Observation */
	public void checkTrackOneMatch_PERSON() {
		List<Observation> obsList = frontend.trackMatch(TrackMatchRequest.newBuilder()
				.setObject(persons.get(0))
				.build())
				.getObservationsList();
		assertTrue(obsList.size() == 1 &&
				obsList.contains(observations.get(0)));
	}

	@Test
	/* Check CAR TrackMatch Observation */
	public void checkTrackOneMatch_CAR() {
		List<Observation> obsList = frontend.trackMatch(TrackMatchRequest.newBuilder()
				.setObject(cars.get(0))
				.build())
				.getObservationsList();
		assertTrue(obsList.size() == 1 &&
				obsList.contains(observations.get(2)));
	}

	@Test
	/* Check Pattern '[0-9]*0' */
	public void checkTrackMatchPattern_L() {
		ObservedObject obj = ObservedObject.newBuilder()
				.setType(Type.PERSON)
				.setId("*0")
				.build();
		List<Observation> obsList = frontend.trackMatch(TrackMatchRequest.newBuilder()
				.setObject(obj)
				.build())
				.getObservationsList();
		assertTrue(obsList.size() == 1 &&
                obsList.contains(observations.get(0)));
	}

	@Test
	/* Check Pattern '[0-9]*' */
	public void checkTrackMatchPattern_R() {
		ObservedObject obj = ObservedObject.newBuilder()
				.setType(Type.PERSON)
				.setId("*")
				.build();
		List<Observation> obsList = frontend.trackMatch(TrackMatchRequest.newBuilder()
				.setObject(obj)
				.build())
				.getObservationsList();
		assertTrue(obsList.size() == 2 &&
				obsList.contains(observations.get(0)) && obsList.contains(observations.get(1)));
	}

	@Test
	/* Check Pattern 'AA[A-Z0-9]*0' */
	public void checkTrackMatchPattern_M() {
		ObservedObject obj = ObservedObject.newBuilder()
				.setType(Type.CAR)
				.setId("AA*0")
				.build();
		List<Observation> obsList = frontend.trackMatch(TrackMatchRequest.newBuilder()
				.setObject(obj)
				.build())
				.getObservationsList();
		assertTrue(obsList.size() == 1 &&
				obsList.contains(observations.get(2)));
	}

	@Test
	/* Check Left & Right '*' */
	/* Check Pattern '[0-9]*1[0-9]*' */
	public void checkTrackMatchPattern_LR() {
		ObservedObject obj = ObservedObject.newBuilder()
				.setType(Type.PERSON)
				.setId("*1*")
				.build();
		List<Observation> obsList = frontend.trackMatch(TrackMatchRequest.newBuilder()
				.setObject(obj)
				.build())
				.getObservationsList();
		assertTrue(obsList.size() == 1 &&
				obsList.contains(observations.get(1)));
	}

	@Test
	/* Check Pattern '[A-Z0-9]*0[A-Z0-9]*0' */
	public void checkTrackMatchPattern_LM() {
		ObservedObject obj = ObservedObject.newBuilder()
				.setType(Type.CAR)
				.setId("*0*0")
				.build();
		List<Observation> obsList = frontend.trackMatch(TrackMatchRequest.newBuilder()
				.setObject(obj)
				.build())
				.getObservationsList();
		assertTrue(obsList.size() == 1 &&
				obsList.contains(observations.get(2)));
	}

	@Test
	/* Check Pattern 'A[A-Z0-9]*0[A-Z0-9]*' */
	public void checkTrackMatchPattern_MR() {
		ObservedObject obj = ObservedObject.newBuilder()
				.setType(Type.CAR)
				.setId("A*0*")
				.build();
		List<Observation> obsList = frontend.trackMatch(TrackMatchRequest.newBuilder()
				.setObject(obj)
				.build())
				.getObservationsList();
		assertTrue(obsList.size() == 2 &&
				obsList.contains(observations.get(2)) && obsList.contains(observations.get(3)));
	}

	@Test
	/* Check NOT_FOUND Exception | Try to TrackMatch a Non Observed Object */
	public void checkTrackMatchEmpty_NOTFOUND() {
		ObservedObject obj = ObservedObject.newBuilder()
				.setType(Type.CAR)
				.setId("B*")
				.build();
		assertEquals(NOT_FOUND, assertThrows(StatusRuntimeException.class, () -> {
			frontend.trackMatch(TrackMatchRequest.newBuilder()
					.setObject(obj)
					.build());
		}).getStatus().getCode());
	}

	/* Trace Tests */
	@Test
	/* Check PERSON Trace Observations & Observations Order */
	public void checkTrace_PERSON() {
		List<Observation> obsList = frontend.trace(TraceRequest.newBuilder()
				.setObject(persons.get(0))
				.build())
				.getObservationsList();
		assertTrue(obsList.size() == 2 &&
				obsList.get(0).equals(observations.get(0)) && obsList.get(1).equals(observations.get(4)));
	}

	@Test
	/* Check CAR Trace Observations & Observations Order */
	public void checkTrace_CAR() {
		List<Observation> obsList = frontend.trace(TraceRequest.newBuilder()
				.setObject(cars.get(0))
				.build())
				.getObservationsList();
		assertTrue(obsList.size() == 2 &&
				obsList.get(0).equals(observations.get(2)) && obsList.get(1).equals(observations.get(6)));
	}

	@Test
	/* Check NOT_FOUND Exception | Try to Trace a Non Observed Object */
	public void checkTraceEmpty_NOTFOUND() {
		ObservedObject obj = ObservedObject.newBuilder()
				.setType(Type.PERSON)
				.setId("3")
				.build();
		assertEquals(NOT_FOUND, assertThrows(StatusRuntimeException.class, () -> {
			frontend.trace(TraceRequest.newBuilder()
					.setObject(obj)
					.build());
		}).getStatus().getCode());
	}

	@Test
	/* Check INVALID_ARGUMENT Exception | Try to Trace an Invalid Observed Object */
	public void checkTraceEmpty_INVALID() {
		ObservedObject obj = ObservedObject.newBuilder()
				.setType(Type.PERSON)
				.setId("AA0000")
				.build();
		assertEquals(INVALID_ARGUMENT, assertThrows(StatusRuntimeException.class, () -> {
			frontend.trace(TraceRequest.newBuilder()
					.setObject(obj)
					.build());
		}).getStatus().getCode());
	}
}
