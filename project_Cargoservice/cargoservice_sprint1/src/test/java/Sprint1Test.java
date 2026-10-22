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
        IApplMessage req = CommUtils.buildRequest(SENDER, id, payload, actor);
        IApplMessage reply = conn.request(req, REQUEST_TIMEOUT_MS);
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
    public void test01_holdCoordinate() throws Exception {
        for (String point : Arrays.asList("ioport", "slot5", "home",
                "slot1", "slot2", "slot3", "slot4")) {
            IApplMessage reply = request("hold", "ask_for_point", "ask_for_point(" + point + ")");
            isReply(reply, "point_found");
            String[] xy = args(reply);
            assertEquals("Coordinate di " + point, 2, xy.length);
            assertTrue("X non finita", Double.isFinite(Double.parseDouble(xy[0])));
            assertTrue("Y non finita", Double.isFinite(Double.parseDouble(xy[1])));
        }
        IApplMessage missing = request("hold", "ask_for_point", "ask_for_point(punto_inesistente)");
        isReply(missing, "point_not_found");
        assertEquals("0", oneArg(missing));
    }

    @Test(timeout = 20000)
    public void test02_holdRispostaValidaIndipendentementeDalloStato() throws Exception {
        // NON presuppone che l'hold sia vuoto: un altro test puo' averlo riempito.
        validSlotOrFull(slot());
    }

    @Test(timeout = 20000)
    public void test03_sonarMock() throws Exception {
        IApplMessage reply = request("sonar_mock", "detect_container", "detect_container(0)");
        isReply(reply, "container_detected");
        assertEquals("0", oneArg(reply));
    }

    @Test(timeout = 45000)
    public void test04_markerProgressivo() throws Exception {
        IApplMessage first = request("marker", "start_marking", "start_marking(0)");
        IApplMessage second = request("marker", "start_marking", "start_marking(0)");
        isReply(first, "container_marked");
        isReply(second, "container_marked");
        String b1 = oneArg(first);
        String b2 = oneArg(second);
        assertTrue("Barcode errato: " + b1, b1.matches("code\\d+"));
        assertTrue("Barcode errato: " + b2, b2.matches("code\\d+"));
        assertTrue("Barcode non progressivi: " + b1 + ", " + b2,
                Integer.parseInt(b2.substring(4)) > Integer.parseInt(b1.substring(4)));
    }

    @Test(timeout = 30000)
    public void test05_cargoserviceContratto() throws Exception {
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
    public void test06_holdDaVuotoAPieno() throws Exception {
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
            dispatch("hold", "container_deployed",
                    "container_deployed(" + assigned + "," + (1000 + i) + ")");

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
    public void test07_caricamentoIntegratoConRobot() throws Exception {
    	
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
    public void test08_richiestaRifiutataConHoldPieno() throws Exception {

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
    public void test09_richiestaDuranteCaricamento_RetryLater() throws Exception {
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

        // 3. Attende brevemente affinché il caricamento
        //    entri nella fase di movimentazione.
        TimeUnit.SECONDS.sleep(2);

        // 4. Seconda richiesta mentre il primo caricamento è in corso
        System.out.println(
            "[SPRINT1 E2E] Invio seconda richiesta durante il caricamento"
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
}
