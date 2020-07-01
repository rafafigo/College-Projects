package pt.tecnico.sauron.silo.client;

import io.grpc.StatusRuntimeException;
import static io.grpc.Status.Code.*;
import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.client.exception.SiloFrontendException;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.util.List;
import static org.junit.Assert.*;


public class SiloCamJoinCamInfoIT extends BaseIT {

    private static SiloFrontend frontend;

	@BeforeAll
    /* Connect to Server */
	public static void oneTimeSetUp() {
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
	}

	@AfterAll
    /* Disconnects with server */
	public static void oneTimeTearDown() {
		frontend.shutdown();
	}

    /* TearDown Server */
	@AfterEach
	public void tearDown() {
		frontend.ctrl_clear(CtrlClearRequest.getDefaultInstance());
	}

	/* Cam_Join Tests */
	@Test
    /* Checks registration of a Camera */
	public void Register1Camera() {
        Location location = Location.newBuilder().setLatitude(23).setLongitude(22).build();
        Camera cam = Camera.newBuilder().setName("Test1").setLocation(location).build();

        frontend.cam_join(CamJoinRequest.newBuilder().setCamera(cam).build());
        CtrlPingResponse response = frontend.ctrl_ping(CtrlPingRequest.getDefaultInstance());
        Camera addedCam = response.getCamerasList().get(0);
        assertEquals(cam, addedCam);
	}

    @Test
    /* Checks the registration of two Cameras with different names */
	public void Register2CamerasDiffNames() {
        Location location = Location.newBuilder().setLatitude(23).setLongitude(22).build();
        Camera cam1 = Camera.newBuilder().setName("cam1").setLocation(location).build();
        Camera cam2 = Camera.newBuilder().setName("cam2").setLocation(location).build();

        frontend.cam_join(CamJoinRequest.newBuilder().setCamera(cam1).build());
        frontend.cam_join(CamJoinRequest.newBuilder().setCamera(cam2).build());

        CtrlPingResponse response = frontend.ctrl_ping(CtrlPingRequest.getDefaultInstance());
        List<Camera> cameras = response.getCamerasList();
        assertTrue(cameras.contains(cam1));
        assertTrue(cameras.contains(cam2));
    }

    @Test
    /* Checks if the registration of the same Camera two times, registers only one time */
    public void Register2CamerasSameNameSameLocation() {
        Location location = Location.newBuilder().setLatitude(23).setLongitude(22).build();
        Camera cam = Camera.newBuilder().setName("cam").setLocation(location).build();
        frontend.cam_join(CamJoinRequest.newBuilder().setCamera(cam).build());
        frontend.cam_join(CamJoinRequest.newBuilder().setCamera(cam).build());

        CtrlPingResponse response = frontend.ctrl_ping(CtrlPingRequest.getDefaultInstance());
        List<Camera> cameras = response.getCamerasList();
        assertEquals(1, cameras.size());
    }

    @Test
    /* Check if the registration of two cameras with same name and different location throws an exception with code ALREADY_EXISTS */
    public void Register2CamerasSameNameDiffLocation() {
        Location location1 = Location.newBuilder().setLatitude(23).setLongitude(22).build();
        Location location2 = Location.newBuilder().setLatitude(25).setLongitude(29).build();
        Camera cam1 = Camera.newBuilder().setName("cam").setLocation(location1).build();
        Camera cam2 = Camera.newBuilder().setName("cam").setLocation(location2).build();

        frontend.cam_join(CamJoinRequest.newBuilder().setCamera(cam1).build());

        assertEquals(ALREADY_EXISTS, Assertions.assertThrows(StatusRuntimeException.class, () ->
                frontend.cam_join(CamJoinRequest.newBuilder().setCamera(cam2).build())).getStatus().getCode());
    }

    @Test
    /* Check if the registration of a camera with an invalid name (Less then 3 Characters) throws an exception with code INVALID_ARGUMENT */
    public void RegisterCameraInvalidName2Characters() {
        Location location = Location.newBuilder().setLatitude(23).setLongitude(22).build();
        Camera cam = Camera.newBuilder().setName("ca").setLocation(location).build();

        assertEquals(INVALID_ARGUMENT, Assertions.assertThrows(StatusRuntimeException.class, () ->
                frontend.cam_join(CamJoinRequest.newBuilder().setCamera(cam).build())).getStatus().getCode());
    }

    @Test
    /* Check if the registration of a camera with an invalid name (More then 15 Characters) throws an exception with code INVALID_ARGUMENT */
    public void RegisterCameraInvalidName16Characters() {
        Location location = Location.newBuilder().setLatitude(23).setLongitude(22).build();
        Camera cam = Camera.newBuilder().setName("xxxxxxxxxxxxxxxx").setLocation(location).build();

        assertEquals(INVALID_ARGUMENT, Assertions.assertThrows(StatusRuntimeException.class, () ->
                frontend.cam_join(CamJoinRequest.newBuilder().setCamera(cam).build())).getStatus().getCode());
    }

    @Test
    /* Check if the registration of a camera with an invalid name (Non Alphanumeric Characters) throws an exception with code INVALID_ARGUMENT */
    public void RegisterCameraInvalidNameNonAlphanumericCharacters() {
        Location location = Location.newBuilder().setLatitude(30).setLongitude(30).build();
        Camera cam = Camera.newBuilder().setName("(xx#x$x|x&)").setLocation(location).build();

        assertEquals(INVALID_ARGUMENT, Assertions.assertThrows(StatusRuntimeException.class, () ->
                frontend.cam_join(CamJoinRequest.newBuilder().setCamera(cam).build())).getStatus().getCode());
    }

    @Test
    /* Check if the registration of a camera with an invalid latitude (More then 90) throws an exception with code INVALID_ARGUMENT */
    public void RegisterCameraInvalidLatitudeMore() {
        Location location = Location.newBuilder().setLatitude(91).setLongitude(22).build();
        Camera cam = Camera.newBuilder().setName("cam").setLocation(location).build();

        assertEquals(INVALID_ARGUMENT, Assertions.assertThrows(StatusRuntimeException.class, () ->
                frontend.cam_join(CamJoinRequest.newBuilder().setCamera(cam).build())).getStatus().getCode());
    }

    @Test
    /* Check if the registration of a camera with an invalid latitude (Less then -90) throws an exception with code INVALID_ARGUMENT */
    public void RegisterCameraInvalidLatitudeLess() {
        Location location = Location.newBuilder().setLatitude(-91).setLongitude(22).build();
        Camera cam = Camera.newBuilder().setName("cam").setLocation(location).build();

        assertEquals(INVALID_ARGUMENT, Assertions.assertThrows(StatusRuntimeException.class, () ->
                frontend.cam_join(CamJoinRequest.newBuilder().setCamera(cam).build())).getStatus().getCode());
    }

    @Test
    /* Check if the registration of a camera with an invalid longitude (More then 180) throws an exception with code INVALID_ARGUMENT */
    public void RegisterCameraInvalidLongitudeMore() {
        Location location = Location.newBuilder().setLatitude(23).setLongitude(181).build();
        Camera cam = Camera.newBuilder().setName("cam").setLocation(location).build();

        assertEquals(INVALID_ARGUMENT, Assertions.assertThrows(StatusRuntimeException.class, () ->
                frontend.cam_join(CamJoinRequest.newBuilder().setCamera(cam).build())).getStatus().getCode());
    }

    @Test
    /* Check if the registration of a camera with an invalid longitude (Less then -180) throws an exception with code INVALID_ARGUMENT */
    public void RegisterCameraInvalidLongitudeLess() {
        Location location = Location.newBuilder().setLatitude(23).setLongitude(-181).build();
        Camera cam = Camera.newBuilder().setName("cam").setLocation(location).build();

        assertEquals(INVALID_ARGUMENT, Assertions.assertThrows(StatusRuntimeException.class, () ->
                frontend.cam_join(CamJoinRequest.newBuilder().setCamera(cam).build())).getStatus().getCode());
    }

    @Test
    /* Check if the registration without setting anything throws an exception with code INVALID_ARGUMENT */
    public void RegisterCameraNoCamera() {
        assertEquals(INVALID_ARGUMENT, Assertions.assertThrows(StatusRuntimeException.class, () ->
                frontend.cam_join(CamJoinRequest.getDefaultInstance())).getStatus().getCode());
    }

    @Test
    /* Check if the registration without setting Location throws an exception with code INVALID_ARGUMENT */
    public void RegisterCameraNoLocation() {
        Camera cam = Camera.newBuilder().setName("cam").build();

        assertEquals(INVALID_ARGUMENT, Assertions.assertThrows(StatusRuntimeException.class, () ->
                frontend.cam_join(CamJoinRequest.newBuilder().setCamera(cam).build())).getStatus().getCode());
    }

    /* Cam_Info Tests */

    @Test
    /* Check if the Location of a Camera that was registered is correct*/
    public void CamInfoOfRegisteredCamera() {
        Location location1 = Location.newBuilder().setLatitude(23).setLongitude(22).build();
        Camera cam = Camera.newBuilder().setName("cam").setLocation(location1).build();
        frontend.cam_join(CamJoinRequest.newBuilder().setCamera(cam).build());

        CamInfoResponse response = frontend.cam_info(CamInfoRequest.newBuilder().setName("cam").build());
        assertEquals(cam.getLocation(), response.getLocation());
    }

    @Test
    /* Check if the search of cam_info of a camera with an invalid name (Less then 3 Characters) throws an exception with code INVALID_ARGUMENT */
    public void CamInfoOfInvalidName2Characters() {
	    assertEquals(INVALID_ARGUMENT, Assertions.assertThrows(StatusRuntimeException.class, () ->
                frontend.cam_info(CamInfoRequest.newBuilder().setName("ca").build())).getStatus().getCode());
    }

    @Test
    /* Check if the search of cam_info of a camera with an invalid name (more then 16 Characters) throws an exception with code INVALID_ARGUMENT */
    public void CamInfoOfInvalidName16Characters() {
        assertEquals(INVALID_ARGUMENT, Assertions.assertThrows(StatusRuntimeException.class, () ->
                frontend.cam_info(CamInfoRequest.newBuilder().setName("xxxxxxxxxxxxxxxx").build())).getStatus().getCode());
    }

    @Test
    /* Check if the search of cam_info of a camera not registered throws an exception with code NOT_FOUND */
    public void CamInfoOfCameraNotRegistered() {
        assertEquals(NOT_FOUND,Assertions.assertThrows(StatusRuntimeException.class, () ->
                frontend.cam_info(CamInfoRequest.newBuilder().setName("cam").build())).getStatus().getCode());
    }
}
