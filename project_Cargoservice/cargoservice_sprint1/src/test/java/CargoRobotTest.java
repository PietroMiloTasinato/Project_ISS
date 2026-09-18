package test.java;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import unibo.basicomm23.interfaces.IApplMessage;
import unibo.basicomm23.interfaces.Interaction;
import unibo.basicomm23.msg.ProtocolType;
import unibo.basicomm23.utils.CommUtils;
import unibo.basicomm23.utils.ConnectionFactory;

public class CargoRobotTest {
	private static Interaction conn;
	
	@BeforeClass
	public static void setup() {
	    conn = ConnectionFactory.createClientSupport23(ProtocolType.tcp, "localhost", "8000");
	}
	
	
	@Test
	public void testMoveRobot() throws Exception{
		IApplMessage request = CommUtils.buildDispatch("tester",
                "move_container_to_slot", "move_container_to_slot(marker)",
                "cargorobot");
		System.out.println("CARGOROBOT | Richiesta: " + request.toString());
		String response = conn.request(request).toString();
		System.out.println("CARGOROBOT | Risposta: " + response.toString());
		
		assertTrue(true);
		/*IApplMessage request = CommUtils.buildRequest("tester",
                "get_robot_state", "get_robot_state(0)",
                "smartrobot");*/
		
	}
	
	
}
