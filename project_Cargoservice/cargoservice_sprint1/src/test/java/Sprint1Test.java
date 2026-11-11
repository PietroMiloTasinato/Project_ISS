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
        Set<String> used = new HashSet<>();
        IApplMessage initial = slot();
        isReply(initial, "slot_assigned"); // qui, e solo qui, verifichiamo l'hold vuoto

        for (int i = 1; i <= 4; i++) {
            IApplMessage free = i == 1 ? initial : slot();
            isReply(free, "slot_assigned");
            String assigned = oneArg(free);
            validSlot(assigned);
            assertTrue("Slot assegnato due volte: " + assigned, used.add(assigned));
            dispatch("hold", "change_slot_status",
                    "change_slot_status(OCCUPIED)");

            // Il dispatch e' asincrono: attendere una risposta diversa dal vecchio slot.
            long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            boolean updated = false;
            while (System.nanoTime() < until) {
                IApplMessage next = slot();
                validSlotOrFull(next);
                if ("hold_full".equals(next.msgId())
                        || !assigned.equals(oneArg(next))) {
                    updated = true;
                    break;
                }
                Thread.sleep(100);
            }
            assertTrue("Lo slot non risulta occupato: " + assigned, updated);
        }
        assertEquals(4, used.size());
        IApplMessage full = slot();
        isReply(full, "hold_full");
        assertEquals("0", oneArg(full));
    }

    @Test(timeout = 150000)
    public void test_caricamentoIntegratoConRobot() throws Exception {
    	
    	System.out.println("[SPRINT1 E2E] Avvio test caricamento con robot");

        // 1. Verifica che l'hold abbia uno slot libero
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
        // 3. Attende fino a 120 secondi che il caricamento venga completato
        long deadline =
            System.nanoTime() + TimeUnit.SECONDS.toNanos(120);
        boolean containerLoaded = false;
        while (System.nanoTime() < deadline) {
            TimeUnit.SECONDS.sleep(2);
            // Controlla lo stato dell'hold
            IApplMessage after = slot();
            validSlotOrFull(after);
            if ("hold_full".equals(after.msgId())) {

                containerLoaded = true;

                System.out.println(
                    "[SPRINT1 E2E] Hold pieno dopo il caricamento"
                );

                break;
            }

            String currentFree = oneArg(after);
            System.out.println(
                "[SPRINT1 E2E] Slot attualmente libero: " + currentFree
            );

            // Se cambia il primo slot libero,
            // almeno un container è stato registrato
            if (!initialFree.equals(currentFree)) {
                containerLoaded = true;
                System.out.println(
                    "[SPRINT1 E2E] Container registrato correttamente!"
                );
                System.out.println(
                    "[SPRINT1 E2E] Slot precedente: " + initialFree
                );
                System.out.println(
                    "[SPRINT1 E2E] Nuovo slot libero: " + currentFree
                );
                break;
            }
        }
        // 4. Verifica finale
        assertTrue(
            "ERRORE: nessun container registrato nell'hold entro 120 secondi. "
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

        // 1. Verifica che ci sia almeno uno slot libero
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
    
    /**
     * Ciclo completo: 5 pressioni del pulsante. Le prime 4 devono portare al deposito di un container ciascuna
     * (uno per slot); la quinta trova la hold piena e viene rifiutata.
     *
     * press_button e' un dispatch: il test non riceve la reply, quindi verifica
     * gli effetti sulla hold. Il rifiuto della quinta richiesta è visibile nella
     * console QAK: "IOPORT_MOCK | Request rejected" / "IOPORT_MOCK | Hold is full".
     *
     * Test DESTRUTTIVO: richiede un sistema appena avviato (hold vuota).
     */
    @Test(timeout = 420000)
    public void test_cicloCompleto_QuattroCarichiEQuintoRifiutato() throws Exception {
        final int slotCount = 4;
        final int cycleTimeoutMs = 120000;

        System.out.println("[SPRINT1 E2E] Avvio ciclo completo: 4 carichi + 1 richiesta con hold piena");

        Set<String> usedSlots = new HashSet<>();

        for (int i = 1; i <= slotCount; i++) {
            // Slot che la hold indica come libero: sara' quello riservato al carico.
            IApplMessage before = slot();
            isReply(before, "slot_assigned");
            String expected = oneArg(before);
            validSlot(expected);
            assertTrue("Slot gia' usato: " + expected, usedSlots.add(expected));

            System.out.println("[SPRINT1 E2E] Pressione " + i + " di " + slotCount
                    + " (slot atteso: " + expected + ")");
            // Se il ciclo precedente sta ancora riportando il robot a home,
            // la richiesta resta in coda nel cargoservice (IOPort già libera).
            dispatch("io_port", "press_button", "press_button(0)");

            // Attende che il container sia depositato: la hold smette di proporre lo slot.
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(cycleTimeoutMs);
            boolean deployed = false;
            while (System.nanoTime() < deadline) {
                TimeUnit.SECONDS.sleep(1);
                IApplMessage next = slot();
                validSlotOrFull(next);
                if ("hold_full".equals(next.msgId()) || !expected.equals(oneArg(next))) {
                    deployed = true;
                    break;
                }
            }
            assertTrue("Container non registrato nello slot " + expected
                    + " entro " + cycleTimeoutMs / 1000 + " secondi", deployed);
            System.out.println("[SPRINT1 E2E] Container " + i + " depositato in " + expected);
        }

        assertEquals("Ogni carico deve occupare uno slot diverso", slotCount, usedSlots.size());

        // La hold e' piena.
        IApplMessage full = slot();
        isReply(full, "hold_full");
        assertEquals("0", oneArg(full));
        System.out.println("[SPRINT1 E2E] Hold piena dopo " + slotCount + " carichi");

        // Quinta pressione: deve essere rifiutata.
        System.out.println("[SPRINT1 E2E] Quinta pressione del pulsante");
        dispatch("io_port", "press_button", "press_button(0)");

        // Il robot sta ancora tornando a home: la richiesta viene valutata solo
        // quando il cargoservice torna in disengaged.
        TimeUnit.SECONDS.sleep(15);

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
