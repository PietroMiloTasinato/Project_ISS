package test.java;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import unibo.basicomm23.interfaces.IApplMessage;
import unibo.basicomm23.interfaces.Interaction;
import unibo.basicomm23.msg.ProtocolType;
import unibo.basicomm23.utils.CommUtils;
import unibo.basicomm23.utils.ConnectionFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class Sprint1Test {
    private static final String HOST = "localhost";
    private static final int PORT = 8000;
    private static final String SENDER = "sprint1test";
    private static final int REQUEST_TIMEOUT_MS = 12000;

    private Interaction conn;

    @Before
    public void connect() {
        conn = ConnectionFactory.createClientSupport23(
                ProtocolType.tcp, HOST, Integer.toString(PORT));
        assertNotNull("Connessione TCP non creata verso " + HOST + ":" + PORT, conn);
    }

    @After
    public void disconnect() throws Exception {
        if (conn != null) conn.close();
    }

    private IApplMessage request(String actor, String id, String payload) throws Exception {
        return request(actor, id, payload, REQUEST_TIMEOUT_MS);
    }

    private IApplMessage request(String actor, String id, String payload, int timeoutMs) throws Exception {
        IApplMessage req = CommUtils.buildRequest(SENDER, id, payload, actor);
        IApplMessage reply = conn.request(req, timeoutMs);
        assertNotNull("Reply mancante: " + req, reply);
        System.out.println("[SPRINT1] " + actor + " -> " + reply);
        return reply;
    }

    private void dispatch(String actor, String id, String payload) throws Exception {
        IApplMessage msg = CommUtils.buildDispatch(SENDER, id, payload, actor);
        conn.forward(msg);
        System.out.println("[SPRINT1] " + actor + " <- " + msg);
    }

    private IApplMessage slot() throws Exception {
        return request("hold", "ask_for_slot", "ask_for_slot(0)");
    }

    private static void isReply(IApplMessage reply, String id) {
        assertEquals("Reply inattesa: " + reply, id, reply.msgId());
    }

    private static String[] args(IApplMessage reply) {
        String content = reply.msgContent().trim();
        int first = content.indexOf('(');
        int last = content.lastIndexOf(')');
        assertTrue("Payload non conforme: " + content, first >= 0 && last > first);
        String inside = content.substring(first + 1, last);
        String[] parts = inside.split(",", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim().replaceAll("^[\\\"']|[\\\"']$", "");
        }
        return parts;
    }

    private static String oneArg(IApplMessage reply) {
        String[] values = args(reply);
        assertEquals("Mi aspetto un solo argomento: " + reply, 1, values.length);
        return values[0];
    }

    private static void validSlot(String value) {
        assertTrue("Slot non valido: " + value,
                Arrays.asList("slot1", "slot2", "slot3", "slot4").contains(value));
    }

    private void setSlotStatus(String slotId, String status) throws Exception {
        // Dispatch verso hold, come fa il cargoservice. I messaggi sulla stessa connessione
        // sono gestiti in ordine; la breve attesa evita corse con la request successiva.
        dispatch("hold", "change_slot_status", "change_slot_status(" + slotId + "," + status + ")");
        TimeUnit.MILLISECONDS.sleep(300);
    }

    private static void validSlotOrFull(IApplMessage reply) {
        if ("slot_assigned".equals(reply.msgId())) {
            validSlot(oneArg(reply));
        } else {
            isReply(reply, "hold_full");
            assertEquals("0", oneArg(reply));
        }
    }

    @Test(timeout = 30000)
    public void test_holdCoordinate() throws Exception {
        for (String point : Arrays.asList("ioport", "slot5", "home",
                "slot1", "slot2", "slot3", "slot4")) {
            IApplMessage reply = request("hold", "ask_for_point", "ask_for_point(" + point + ")");
            isReply(reply, "point_found");
            String[] xy = args(reply);
            assertEquals("Coordinate di " + point, 3, xy.length);
            assertTrue("X non finita", Double.isFinite(Double.parseDouble(xy[0])));
            assertTrue("Y non finita", Double.isFinite(Double.parseDouble(xy[1])));
        }
        IApplMessage missing = request("hold", "ask_for_point", "ask_for_point(punto_inesistente)");
        isReply(missing, "point_not_found");
        assertEquals("0", oneArg(missing));
    }

    @Test(timeout = 20000)
    public void test_holdRispostaValidaIndipendentementeDalloStato() throws Exception {
        // NON presuppone che l'hold sia vuoto: un altro test puo' averlo riempito.
        // ask_for_slot e' in sola lettura: non modifica la hold.
        validSlotOrFull(slot());
    }

    @Test(timeout = 20000)
    public void test_sonarMock() throws Exception {
        IApplMessage reply = request("sonar_mock", "detect_container", "detect_container(0)");
        isReply(reply, "container_detected");
        assertEquals("0", oneArg(reply));
    }

    @Test(timeout = 30000)
    public void test_cargoserviceContratto() throws Exception {
        // Da eseguire isolatamente: se engaged, questa richiesta avvia davvero la movimentazione.
        IApplMessage reply = request("cargoservice", "request_to_load", "request_to_load(0)");
        String id = reply.msgId();
        assertTrue("Reply non prevista: " + reply,
                Arrays.asList("engaged", "retrylater", "rejected").contains(id));
        if ("engaged".equals(id)) validSlot(oneArg(reply));
        else assertEquals("0", oneArg(reply));
        System.out.println("[SPRINT1] La sola validita' della reply NON prova il caricamento.");
    }

    @Test(timeout = 60000)
    public void test_holdDaVuotoAPieno() throws Exception {
        // Test DESTRUTTIVO: usare solo un'istanza appena avviata e senza altri carichi.
        // Riproduce il ciclo di vita reale di uno slot, come fa il cargoservice:
        //   ask_for_slot (sola lettura)         -> propone il primo slot FREE
        //   change_slot_status(slot,BOOKED)     -> FREE   -> BOOKED   (prenotazione)
        //   change_slot_status(slot,OCCUPIED)   -> BOOKED -> OCCUPIED (container depositato dal DDR)
        // La hold e' piena solo quando tutti e 4 gli slot sono OCCUPIED.
        Set<String> used = new HashSet<>();

        for (int i = 1; i <= 4; i++) {
            IApplMessage free = slot();
            isReply(free, "slot_assigned"); // la hold parte vuota: i primi 4 slot sono liberi
            String assigned = oneArg(free);
            validSlot(assigned);
            assertTrue("Slot assegnato due volte: " + assigned, used.add(assigned));

            // ask_for_slot non modifica la hold: ripetuto, propone lo stesso slot.
            assertEquals("ask_for_slot deve essere in sola lettura", assigned, oneArg(slot()));

            // Prenotazione: lo slot non e' piu' proposto.
            setSlotStatus(assigned, "BOOKED");
            IApplMessage afterBooking = slot();
            validSlotOrFull(afterBooking);
            if ("slot_assigned".equals(afterBooking.msgId())) {
                assertFalse("Slot BOOKED ancora proposto: " + assigned,
                        assigned.equals(oneArg(afterBooking)));
            }

            // Deposito del container: lo slot prenotato diventa OCCUPIED.
            setSlotStatus(assigned, "OCCUPIED");
        }
        assertEquals(4, used.size());
        IApplMessage full = slot();
        isReply(full, "hold_full");
        assertEquals("0", oneArg(full));
    }

    @Test(timeout = 150000)
    public void test_caricamentoIntegratoConRobot() throws Exception {
    	
    	System.out.println("[SPRINT1 E2E] Avvio test caricamento con robot");

        // 1. Verifica che l'hold abbia uno slot libero (ask_for_slot e' in sola lettura):
        //    sara' lo slot che il cargoservice riservera' alla pressione del pulsante.
        IApplMessage before = slot();
        isReply(before, "slot_assigned");
        String initialFree = oneArg(before);
        validSlot(initialFree);
        System.out.println(
            "[SPRINT1 E2E] Slot inizialmente libero: " + initialFree
        );
        // 2. Simula la pressione del pulsante dell'IOPort
        System.out.println(
            "[SPRINT1 E2E] Invio press_button(0) a io_port"
        );
        dispatch(
            "io_port",
            "press_button",
            "press_button(0)"
        );
        System.out.println(
            "[SPRINT1 E2E] Pulsante premuto. Attendo il caricamento..."
        );
        // 3. Attende la fine del ciclo di carico con un tempo fisso: ask_for_slot non permette
        //    di distinguere BOOKED da OCCUPIED, quindi non c'e' un segnale di fine ciclo da sondare.
        TimeUnit.MILLISECONDS.sleep(CYCLE_WAIT_MS);

        // 4. Verifica finale: lo slot iniziale non e' piu' libero (prenotato/occupato dal cargoservice),
        //    quindi la hold propone uno slot diverso oppure e' piena.
        //    Il passaggio a OCCUPIED va controllato nella console QAK:
        //    "CARGOSERVICE | Container deployed".
        IApplMessage after = slot();
        validSlotOrFull(after);
        boolean containerLoaded = "hold_full".equals(after.msgId())
                || !initialFree.equals(oneArg(after));
        assertTrue(
            "ERRORE: lo slot " + initialFree + " risulta ancora libero dopo "
            + CYCLE_WAIT_MS / 1000 + " secondi. "
            + "Controllare le console di cargoservice e robotsmart.",
            containerLoaded
        );
        System.out.println(
            "[SPRINT1 E2E] TEST SUPERATO: caricamento completato"
        );
    }
    
    @Test(timeout = 20000)
    public void test_richiestaRifiutataConHoldPieno() throws Exception {

        System.out.println("[SPRINT1 E2E] Verifica hold pieno");

        // Precondizione: tutti e quattro gli slot devono essere occupati.
        IApplMessage before = slot();

        isReply(before, "hold_full");
        assertEquals("0", oneArg(before));

        System.out.println("[SPRINT1 E2E] Hold pieno confermato");

        // La richiesta attraversa il vero QActor io_port.
        dispatch(
            "io_port",
            "press_button",
            "press_button(0)"
        );

        System.out.println("[SPRINT1 E2E] Quinta pressione del pulsante inviata");

        // Verifica che l'hold rimanga pieno.
        Thread.sleep(2000);

        IApplMessage after = slot();

        isReply(after, "hold_full");
        assertEquals("0", oneArg(after));

        System.out.println("[SPRINT1 E2E] Hold ancora pieno");
        System.out.println(
            "[SPRINT1 E2E] Verificare nella console QAK: IOPORT_MOCK | Request rejected"
        );
    }
    
    @Test(timeout = 60000)
    public void test_richiestaDuranteCaricamento_RetryLater() throws Exception {
        System.out.println(
            "[SPRINT1 E2E] Avvio test richiesta durante caricamento"
        );

        // 1. Verifica che ci sia almeno uno slot libero (ask_for_slot e' in sola lettura):
        //    sara' lo stesso slot che il cargoservice assegnera' alla prima richiesta.
        IApplMessage before = slot();

        isReply(before, "slot_assigned");

        String initialSlot = oneArg(before);
        validSlot(initialSlot);

        System.out.println(
            "[SPRINT1 E2E] Slot disponibile: " + initialSlot
        );

        // 2. Prima richiesta: deve essere accettata
        IApplMessage firstReply = request(
            "cargoservice",
            "request_to_load",
            "request_to_load(0)"
        );

        isReply(firstReply, "engaged");

        String assignedSlot = oneArg(firstReply);
        validSlot(assignedSlot);

        assertEquals(
            "Lo slot assegnato non corrisponde a quello disponibile",
            initialSlot,
            assignedSlot
        );

        System.out.println(
            "[SPRINT1 E2E] Prima richiesta accettata: " + assignedSlot
        );

        // 3. Il sonar mock risponde subito: l'IOPort risulta occupata fino a
        //    quando il robot non preleva il container. Attende solo il tempo
        //    necessario perché il cargoservice sia in attesa del robot.
        TimeUnit.MILLISECONDS.sleep(500);

        // 4. Seconda richiesta mentre l'IOPort è occupata
        System.out.println(
            "[SPRINT1 E2E] Invio seconda richiesta con IOPort occupata"
        );

        IApplMessage secondReply = request(
            "cargoservice",
            "request_to_load",
            "request_to_load(0)"
        );

        // 5. La seconda richiesta deve ricevere retrylater
        isReply(secondReply, "retrylater");

        assertEquals(
            "0",
            oneArg(secondReply)
        );

        System.out.println(
            "[SPRINT1 E2E] Seconda richiesta correttamente rifiutata temporaneamente"
        );

        System.out.println(
            "[SPRINT1 E2E] Risposta ricevuta: retrylater(0)"
        );

        System.out.println(
            "[SPRINT1 E2E] TEST SUPERATO!"
        );
    }
    
    // Durata massima attesa per un ciclo di carico completo
    private static final int CYCLE_WAIT_MS = 50000;

    /**
     * Ciclo completo: 5 pressioni del pulsante IOPort. Le prime 4 portano al deposito di un
     * container ciascuna (uno per slot); la quinta trova la hold piena e viene rifiutata
     * (console QAK: "IOPORT_MOCK | Request rejected" / "IOPORT_MOCK | Hold is full").
     *
     * NOTA: ask_for_slot e' in sola lettura, ma non distingue BOOKED da OCCUPIED, quindi non
     * segnala la fine di un ciclo. Il test attende un tempo fisso (CYCLE_WAIT_MS) dopo ogni
     * pressione, in modo che il ciclo sia concluso: una pressione con IOPort occupata
     * riceve retrylater e IOPORT_MOCK la scarta.
     * La hold si sonda solo alla fine, quando deve essere piena.
     *
     * Test DESTRUTTIVO: richiede un sistema appena avviato (hold vuota).
     */
    @Test(timeout = 420000)
    public void test_cicloCompleto_QuattroCarichiEQuintoRifiutato() throws Exception {
        final int slotCount = 4;

        System.out.println("[SPRINT1 E2E] Avvio ciclo completo: 4 carichi + 1 richiesta con hold piena");

        for (int i = 1; i <= slotCount; i++) {
            System.out.println("[SPRINT1 E2E] Pressione " + i + " di " + slotCount
                    + ", attendo " + CYCLE_WAIT_MS / 1000 + " s per il ciclo di carico prima di una nuova pressione del pulsante");
            dispatch("io_port", "press_button", "press_button(0)");
            TimeUnit.MILLISECONDS.sleep(CYCLE_WAIT_MS);
        }

        // La hold deve essere piena. Se non lo e', ask_for_slot restituisce slot_assigned
        // e il test fallisce, tipicamente perche' CYCLE_WAIT_MS e' troppo corto.
        IApplMessage full = slot();
        isReply(full, "hold_full");
        assertEquals("0", oneArg(full));
        System.out.println("[SPRINT1 E2E] Hold piena dopo " + slotCount + " carichi");

        // Quinta pressione: deve essere rifiutata.
        System.out.println("[SPRINT1 E2E] Quinta pressione del pulsante");
        dispatch("io_port", "press_button", "press_button(0)");
        TimeUnit.MILLISECONDS.sleep(CYCLE_WAIT_MS);

        // La richiesta rifiutata non modifica la hold.
        IApplMessage after = slot();
        isReply(after, "hold_full");
        assertEquals("0", oneArg(after));

        System.out.println("[SPRINT1 E2E] Hold ancora piena dopo la quinta richiesta");
        System.out.println("[SPRINT1 E2E] Verificare nella console QAK: "
                + "IOPORT_MOCK | Request rejected / Hold is full");
        System.out.println("[SPRINT1 E2E] TEST SUPERATO: ciclo completo");
    }
}
