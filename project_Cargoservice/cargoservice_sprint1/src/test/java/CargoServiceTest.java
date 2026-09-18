/*package test.java;

import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.BeforeClass;

import unibo.basicomm23.interfaces.Interaction;
import unibo.basicomm23.msg.ProtocolType;
import unibo.basicomm23.utils.CommUtils;
import unibo.basicomm23.utils.ConnectionFactory;
import unibo.basicomm23.interfaces.IApplMessage;

import java.util.concurrent.TimeUnit;

public class CargoServiceTest {
	private static Interaction conn;

	@BeforeClass
	public static void setup() {
	    conn = ConnectionFactory.createClientSupport23(ProtocolType.tcp, "localhost", "8000");
	}
	
	
    // Scenario di test 1: Richiesta di carico accettata da cargoservice
    @Test
    public void testLoadRequestAccepted() throws Exception {
        //Costruzione di richiesta 

        IApplMessage requestStr = CommUtils.buildRequest("tester",
                "load_container", "load_container(\"args\")",
                "cargoservice_handler");
        
        System.out.println("Richiesta: " + requestStr.toString());
        
        //Risposta accettata perchè robot e marker sono liberi
        String response = conn.request(requestStr).toString();
        
        System.out.println("Risposta: " + response); // Risposta contenente lo slot libero dove posizionare il container
        
        //Verifica che sia stata accettata
        assertTrue("TEST: richiesta accettata", 
                 response.contains("load_accepted"));
        
        TimeUnit.SECONDS.sleep(1); // wait for robot to finish
    }
	
    //Scenario di test 2: Doppia richiesta di carico 
    
    @Test
    public void testDoubleLoadRequest() throws Exception {
	    // Costruzione della prima richiesta 
	    String request1 = CommUtils.buildRequest("tester",
	            "load_container", "load_container(\"args\")", 
	            "cargoservice_handler").toString();
	
	    //Risposta accettata perchè robot e marker sono liberi
	    String response1 = conn.request(request1);
	    
	    System.out.println("Risposta: " + response1); // Risposta contenente lo slot libero dove posizionare il container
	    assertTrue("TEST: Prima richiesta accettata", 
	             response1.contains("load_accepted")); 
	    
	   // Costruzione della seconda richiesta
	    String request2 = CommUtils.buildRequest("tester",
	            "load_container", "load_container(\"args\")", 
	            "cargoservice_handler").toString();
	
	    //Risposta negativa perchè robot e marker non sono liberi
	    String response2 = conn.request(request2);
	    System.out.println("Risposta: " + response2); // Risposta contenente la causa del rifiuto
	    assertTrue("TEST: Seconda richiesta rifiutata",
	    		response2.contains("load_refused") && 
	    		response2.contains("ioport_occupied"));
    }

/*
    //Scenario di test 3: richiesta di carico rifiutata perchè robot e marker non sono liberi senza arrivo concorrenziale di due load_request
    @Test
    public void testLoadRequestDenied() throws Exception {
    	//Costruzione di richiesta
    	String requestStr = CommUtils.buildRequest("tester",
            "load_container", "load_container(\"args\")",
            "cargoservice").toString();
    
    	System.out.println("Richiesta: " + requestStr);
    
    	//Risposta negativa perchè robot e marker non sono liberi
    	String response = conn.request(requestStr);
    
    	System.out.println("Risposta: " + response); // Risposta contenente la causa del rifiuto 
    
    	//Verifica che sia rifiutata perchè robot e marker non sono liberi
    	assertTrue("TEST: richiesta rifiutata perchè robot e marker non sono liberi",
            response.contains("load_refused") && 
            response.contains("out_of_service"));
    }
   
}
*/