import static org.junit.Assert.assertTrue;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.junit.BeforeClass;
import org.junit.Test;
import unibo.basicomm23.interfaces.IApplMessage;
import unibo.basicomm23.coap.CoapConnection;
import unibo.basicomm23.interfaces.Interaction;
import unibo.basicomm23.msg.ProtocolType;
import unibo.basicomm23.utils.CommUtils;
import unibo.basicomm23.utils.ConnectionFactory;

public class HoldTest {
	
	private static Interaction conn;
	private static CoapConnection holdCoap;
	
	
	@BeforeClass
	public static void setup() {
		System.out.println("TESTER | Starting setup");
	    conn = ConnectionFactory.createClientSupport23(ProtocolType.tcp, "localhost", "8000");
	    holdCoap = (CoapConnection) ConnectionFactory.createClientSupport23(ProtocolType.coap, "localhost:8000", "ctx_cargoservice/hold");
	    System.out.println("TESTER | Setup done");
	}
	
	@Test
	public void testHold() throws Exception {
		holdCoap.observeResource(new CoapHandler() {
			@Override 
			public void onLoad(CoapResponse response) {
				String content = response.getResponseText();
				System.out.println("TESTER | " + content); // Stampa sempre nonews
				assertTrue(content.contains("slot1:code1"));
			}
			
			@Override 
			public void onError() {
				System.out.println("TESTER | COAP ERROR");
			}
				
		});
		conn = ConnectionFactory.createClientSupport23(ProtocolType.tcp, "localhost", "8000");
		IApplMessage request = CommUtils.buildRequest("tester", "ask_for_slot", "ask_for_slot(ARG)", "hold");
		IApplMessage response = conn.request(request);
		IApplMessage request1 = CommUtils.buildDispatch("tester", "container_deployed", "container_deployed(ARG)", "hold");
		conn.forward(request1);
		Thread.sleep(5000);
	}
	
}
