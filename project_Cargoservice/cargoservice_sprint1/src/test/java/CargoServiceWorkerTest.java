
package test.java;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.concurrent.TimeUnit;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapHandler;
import unibo.basicomm23.coap.CoapConnection;
import unibo.basicomm23.interfaces.IApplMessage;
import unibo.basicomm23.interfaces.Interaction;
import unibo.basicomm23.msg.ProtocolType;
import unibo.basicomm23.utils.CommUtils;
import unibo.basicomm23.utils.ConnectionFactory;

/*public class CargoServiceWorkerTest {
	private static Interaction conn;
	private static CoapConnection holdCoap;
	private static CoapConnection markerCoap;
	
	@BeforeClass
	public static void setup() {
		System.out.println("TESTER | Starting setup");
	    conn = ConnectionFactory.createClientSupport23(ProtocolType.tcp, "localhost", "8000");
	    holdCoap = (CoapConnection) ConnectionFactory.createClientSupport23(ProtocolType.coap, "localhost:8000", "ctx_cargoservice/hold");
	    markerCoap = (CoapConnection) ConnectionFactory.createClientSupport23(ProtocolType.coap, "localhost:8000", "ctx_cargoservice/marker");
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
	
	@Test
	public void test() throws Exception {
		IApplMessage request = CommUtils.buildRequest("tester", "start_engage", "start_engage(ARG)", "cargoservice_handler");
		IApplMessage response = conn.request(request);
		
		
		assertEquals("engage_successful", response.msgId());
	}
	
}*/


