
package test.java;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.concurrent.TimeUnit;

import unibo.basicomm23.interfaces.IApplMessage;
import unibo.basicomm23.interfaces.Interaction;
import unibo.basicomm23.msg.ProtocolType;
import unibo.basicomm23.utils.CommUtils;
import unibo.basicomm23.utils.ConnectionFactory;

public class CargoServiceWorkerTest {
	private static Interaction conn;
	
	@BeforeClass
	public static void setup() {
		System.out.println("TESTER | Starting setup");
	    conn = ConnectionFactory.createClientSupport23(ProtocolType.tcp, "localhost", "8000");
	    System.out.println("TESTER | Setup done");
	}
	
	
	@Test
	public void testMoveRobot() throws Exception{
		System.out.println("TESTER | Starting...");
		IApplMessage request = CommUtils.buildDispatch("tester", "startWorking", "startWorking(ARG)", "cargoservice_worker");
		System.out.println("CARGOROBOT | Richiesta: " + request.toString());
		
		conn.forward(request);
		
		TimeUnit.SECONDS.sleep(40);//attesa per il robot
		
		request = CommUtils.buildRequest("tester", "start_marking", "start_marking(ARG)", "marker");
		System.out.println("CARGOROBOT | Richiesta: " + request.toString());
		
		String response = conn.request(request).toString();
		System.out.println("CARGOROBOT | Risposta: " + response.toString());
		
		assertTrue("TEST: Seconda richiesta rifiutata",
	    		response.contains("code2"));
		
	}
	
}
