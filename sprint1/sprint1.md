# Sprint1

Componenti da realizzare/migliorare:

## Cargoservice
-Dire al Cargorobot se c'e' un container in attesa di essere caricato
-Dire al cliente se il cargorobot e' libero
-guidare il cargorobot
-creare una rappresentazione dell'hold (se non c'e' gia')
-Modellare il caso request rifiutata per hold pieno
-il sistema diventa disengaed se dopo 30 sec non c'e' un container
-modellare il caso out of service per distanza troppo elevata dal sonar

## Cargorobt
-due stati di movimento: con e senza container
-comunicazione con il marker 
-carico e scarico del container
-comunicare con il cargoservice

## Marker
-comunicare con il cargorobot
-servizio di marcatura


## Indice

- [Sprint0](#sprint0)
  - [Indice](#indice)
  - [Punto di Partenza](#punto-di-partenza)
  - [Obiettivi](#obiettivi)
  - [Analisi del Problema](#analisi-del-problema)
    - [Componenti Software già implementate](#Componenti-Software-già-implementate)
    - [Componenti software](#componenti-software)
  - [Core business](#core-business)
  - [Architettura di riferimento](#architettura-di-riferimento)
    - [Messaggi](#messaggi)
    - [Diagramma dell'architettura](#diagramma-dellarchitettura)
  - [Piano di test](#piano-di-test)
  - [Piano di lavoro](#piano-di-lavoro)
  - [Team di lavoro e attività specifiche](#team-di-lavoro-e-attività-specifiche)

## Punto di Partenza

![Architettura Sprint 0](../img/sprint0_arch.png)

## Obiettivi

Lo Sprint1 ha lo scopo di implementare il core business del sistema. Nel nostro caso, questo implica l'implementazione di CargoService e Cargorobot. 


## Analisi del Problema

Come detto nello sprint 0, gli attori cargoservice_handler e cargoservice_worker sono i componenti principali del sistema. Cargoservice_handler gestisce l'interazione con l'utente, gestendo l'I/O (pushbutton, display); mentre cargoservice_worker gestisce il funzionamento del cargorobot e monitora lo stato dell'hold.

Quindi, le attività di cargoservice_handler sono le seguenti:

- gestire le richieste di carico dell'utente
- mostrare le informazioni relative allo stato dell'hold sul display 

Mentre le attività di cargoservice_worker sono:

- monitorare lo stato dell'hold e comunicarlo al cargoservice_handler quando richiesto
- gestire il funzionamento del cargoservice robot
- gestire i moduli interni all'hold (sonar e marker) e assicurarne il corretto funzionamento

### Componenti Software già implementate

![Wenv & DDR](../img/CargoBot.png)

Analizziamo più in dettaglio il VirtualRobot fornito dal committente:

Utilizzeremo il servizio WEnv come un virtual environment per simulare un Differential Drive Robot composto da 3 ruote di cui due sono ruote motrici, che si muove all'interno di un rettangolo contenente vari elementi descritti dal committente (slots, marker, sonar,...). Le capacità del robot di navigare l'environment virtuale verrano esplorate in seguito.

La comunicazione con il virtual environment viene effettuata in due modi:
- HTTP POST sulla porta 8090 — sincrono, request/response il servizio WEnv accetta una sola connessione.
- WebSocket sulla porta 8091 — asincrono, fire-and-forget; WEnv accetta più connessionie e il servizio invia un messaggio a tutti i client connessi.

Sintassi per i messaggi al robot sono definiti tramite
cril (concrete-robot interaction language), la sintassi del comando è la seguente:

```
{"robotmove":"CMDMOVE", "time":T}
CMDMOVE ::= turnLeft | turnRight | moveForward | moveBackward | alarm
```

Un comando in esecuzione può essere interrotto solo tramite un'allarme, un qualsiasi altro comando asincrono ricevuto durante l'esecuzione è rifiutato  mentre l'originale continua la sua esecuzione fino alla sua terminazione. In caso di una collisione, il movimento dura comunque un tempo T, ma i WS clients ottengono {"collision":"MOVEID","target":"OBSTACLEID"}.

Per quanto riguarda la semantica dei messaggi asincroni, si mantiene la struttura dei messaggi cril e si utilizzano i dati ricevuti dal server per rilevare la distanza tra gli altri oggetti dell'environment:
({"sonarName":..,"distance":..,"axis":"x|y"})

Il WEnv è deployable attraverso una docker image, anch'essa fornita dal committente. Insieme al WEnv viene fornita una NaiveGui, ovvero un'interfaccia gfrafica con cui comandare direttamente il robot nell ambiente virtuale da tastiera (i tasti w/a/s/d vengono utilizzati per muovere il robot). La NaiveGui permette anche di modificare l'ambiente virtuale: aggiungere oggetti o spostare quelli già presenti, e modificare la velocità del robot.

Il committente ha presentato varie versioni del VirtualRobot, e abbiamo scelto di implementare cargorobot utilizzando lo SmartRobot invece del semplice BasicRobot per sfruttare le capacità dello SmartRobot di navigare il VirtualEnvironment utilizzando un sistema di coordinate invece di richiedere multipli comandi cril. In aggiunta a queste funzionalità, SmartRobot dispone di un'ulteriore Gui oltre a NaiveGui, accedibile appa porta 9085 dopo aver lanciato il container usando il file `vrWithGui26.yaml`. Questa Gui contiene anche una rappresentazione a griglia del Virtual Environment, che viene aggiornata utilizzando la memoria interna del robot, non utilizzando la conologia dei comandi ricevuti dal server. Questa rappresentazione poi verrà usata per individuare le coordinate dove muovere il robot.


![SmartRobot e GridGUI](../img/SmartRobotGrid.png)

Three new nano-services (Qak actors), all initially in one shared runtime context ctxrobotsmart:

robotmnemo — owns the context map and talks directly to the robot hardware/simulator.
planexec — executes a sequence of moves (a "plan") step by step.
robotsmart — the entry point; computes a path-plan to a goal and delegates its execution.

Three-tier architecture (as drawn in the book):

client esterno
   |
robotsmart --(delega step/move/tune/getstate)--> robotmnemo --> robot fisico/virtuale
   |                                                  |
   |                                            sonardata (interrupt)
   |
   `-(delega doplan)--> planexec --(step/move)--> robotmnemo

robotsmart plans and coordinates, planexec executes sequentially, robotmnemo drives the hardware and keeps the context map — none of the three knows the others' internals; everything is message-based.

Key Qak mechanisms used:

QActor as nano-service — robotmnemo/planexec currently share one JVM/Context, but could later be redistributed across contexts without changing their specified behavior.
Delegation — robotsmart delegates step/move/tuneAtHome/getrobotstate to robotmnemo and doplan to planexec, so it's never blocked and can keep serving other clients/events.
Observable resource — robotmnemo exposes state over CoAP via the updateresource primitive.
Interrupt handling — robotmnemo treats the sonardata event as an interrupt: a prioritized, momentary state transition, returning afterward via returnFromInterrupt. Important nuance stressed in the text: Qak "interrupts" are not OS-style preemptive — the event is still only processed at the end of the current state's actions (a Moore-machine semantics); "interrupt" here just means priority/scope, not real-time preemption.

Context map: an integer matrix — 0=presumed-free cell, 1=obstacle, r=robot — sized so each cell equals the robot's own diameter (making it a perfect occupancy grid). Built by gui.MapUtil.createGridFromMapInFile, which parses a row-delimited string like 0000001@0011001@0000101@0011001@0000001@1111111 into an int[][]. (Building the map for a real room is left as an open exercise — the robot would need to explore.)

robotmnemo — message API and behavior:

Request step : step(TIME) → Reply stepdone(V) / stepfailed(DURATION,CAUSE)
Dispatch move : move(M)            Dispatch setrobotstate : setpos(X,Y,D)
Request setdirection : dir(D) → Reply setdirectiondone : pos(PX,PY)
Request getrobotstate → Reply robotstate(POS,DIR)
Request tuneAtHome(X) → Reply tuneDone(X)
Event vrinfo / sonardata (streamed from WEnv)   Event sonaralarm (emitted to external consumers)

Two injected collaborators: rpos (RobotPosUtils, the "mind" — tracks position/direction) and robot (VRObjForQak, the "body" — talks to the virtual/real robot). Core logic:

step — issues a non-blocking robot.forward(T), then races a sonardata interrupt against a vrinfo success/collision event; handleVrinfoMsgReply interprets the result — on collision it replies stepfailed and issues robot.backward(T) to back off and keep the map consistent with physical reality.
handleSonarData — the interrupt path: emits sonaralarm, then returns to the previous state.
dosetrobotdirection — computes needed rotations (planToSetDirection) and executes them.
tuneAtHome — recalibrates by micro-stepping the robot exactly back to HOME, correcting accumulated drift.

planexec — message API and behavior:

Request doplan(PLAN,STEPTIME) → Reply doplandone(ARG) / doplanfailed(PLANTODO)
Dispatch nextmove(M) / nomoremove(M)     Event alarm(X)

Executes a plan (verbose [w,w,l,w,w] or compact "wwlww") by repeatedly issuing moves to robotmnemo. State machine: 'w' → step request (with a configurable ExecDelay); any other letter → move dispatch + 500ms delay + self-dispatched nextmove; exhausted plan → nomoremove → reply doplandone. Three failure paths are distinguished: obstacle-triggered (stepfailed → planinterruptedobstacle, reporting the unexecuted remainder), external-alarm-triggered (planinterrupted, which waits for the in-flight step to finish before replying), and the edge case where the plan was already finished when the alarm arrived.

robotsmart — message API and behavior:

Request buildPlan(PX,PY,TX,TY) → Reply buildPlanDone(PLAN)
Request moverobot(TARGETX,TARGETY,STEPTIME) → Reply moverobotdone(ARG) / moverobotfailed(PLANDONE,PLANTODO)
Dispatch noplan(X)     Dispatch setplanbuildelay : value(V)

Waits for partnerstarted from robotmnemo before wiring up its delegations, then initializes an A* planner (planning.AStarPathfinding). Handles:

buildPlan — direct call into planner.planForGoal(...), returning the move-string.
moverobot (the main flow) — orient down → get current position → compute A* plan to target → if empty, self-dispatch noplan (blocked) → else doplan to planexec → on success reply moverobotdone(ok); on failure compute completed-vs-remaining path and reply moverobotfailed(PathDone, PathTodo).
setplanbuildelay — tunes an artificial delay so the A* search's current candidate path can be visualized live.

Configuration (basicrobotParams.json, read by AStarPathfinding):

json
{"ExecDelay":"120", "PlanDBuildDelay":"0", "FoundPathDelay":"500"}

ExecDelay = gap between consecutive moves; PlanDBuildDelay = visualization delay during A* search; FoundPathDelay = gap between finding a plan and executing it.

Deployment (robotsmart26.yaml, Docker Compose) launches four services: mosquitto (MQTT broker, port 1883), wenv (the virtual robot, ports 8090/8091), robotoutgui25 (context-map GUI, port 8085), and robotsmart26 itself (port 8020). Some ports are listed as both /tcp and /udp because CoAP traffic needs UDP.

Architectural argument — "macro vs. micro": The chapter's broader point is that microservice and actor-based ("nano-service") design are the same computational paradigm applied at two scales:

Macro (Docker level): each service is an isolated container communicating over standard protocols.
Micro (in-JVM level): robotsmart26.qak itself isn't a monolithic block of procedural code but a set of nano-services (Qak actors) sharing one JVM; if one actor fails or is updated, the blast radius is governed by the messaging protocol rather than tight class coupling. Promoting a nano-service into its own container becomes a configuration change, not a logic change.
"Message as contract, beyond Interface": an OOP interface presumes synchronous, shared-memory calls; once the system fragments into micro/nano-services it can no longer govern time, asynchrony, or partial failure — so the real "glue" becomes the message's semantics, not a method signature. Concretely: two nano-services in the same JVM talk over fast internal queues; if a Docker YAML config later moves one onto a separate host, the same dispatches/requests/events just travel over TCP or CoAP instead, with zero code rewrite (no RMI/gRPC needed).
Transparent delegation: in classic OOP, if C calls A which needs help from B, the chain is C→A→B→A→C, with A blocked as a middleman. Here, A can delegate R to B while "passing the baton" of the original sender, so B replies directly to C. This frees A immediately (reducing bottlenecks) and adds resilience (if A crashes after delegating, B↔C can still complete — no domino-effect of a synchronous call chain).

### Componenti software

Per quanto riguarda, i macrocomponenti da sviluppare riportiamo:

- **cargoservice**
- **cargorobot**
- **sonar**
- **IOPort**: composto da un _display_ (web-gui) + _pushbutton_
- **hold** <!-- cosa si intende per stato corrente dell'hold -->
- **led** <!-- chiedere se va tenuto come componente separato o inserirlo dentro IOPort -->

## Architettura di riferimento

Nel modello qak, ogni attore possiede uno stato privato e un proprio comportamento. Gli attori non accedono direttamente allo stato interno degli altri attori, ma coordinano le proprie attività mediante lo scambio di messaggi.

L'assenza di memoria condivisa è considerata un vincolo del modello logico: anche quando più attori vengono eseguiti nella stessa JVM, il loro coordinamento deve avvenire tramite messaggi e non mediante accesso diretto a variabili mutabili condivise.

### Messaggi

Il seguente insieme costituisce una prima definizione dei contratti di interazione. I messaggi potranno essere raffinati durante l'analisi dei singoli sprint.

```
Request load_container : load_container(ARG)
Reply load_accepted : load_accepted(SLOT) for load_container
Reply load_refused : load_refused(CAUSE) for load_container
Reply retrylater : retrylater(CAUSE) for load_container

Dispatch move_container_to_slot : move_container_to_slot(SLOT)

Event container_marked : container_marked(BARCODE)
Event container_detected : container_detected(DISTANCE)
Event sonar_failure : sonar_failure(DISTANCE)

Event show_hold_state : show_hold_state(STATE)
Event show_service_status : show_service_status(STATUS)

```

Le interazioni rimanenti tramite messaggi verranno discusse e modellate durante le fasi di analisi del problema nei successivi sprint.

<!-- CAUSE in load_refused: hold full o system disengaged -->
<!-- STATE in show_hold_state: engaged o disengaged -->
<!-- STATUS in show_service_status: working o out_of_service -->

### Diagramma dell'architettura

Il seguente diagramma rappresenta l'architettura iniziale di riferimento per lo sprint 1.

- ctx_cargoservice: cargoservice, hold, cargorobot, marker, sonar;
- ctx_ioport: ioport, display, pushbutton;
- ctx_devices: led;
- ctx_client: client. <!-- realizza il cliente -->

![Wenv & DDR](/img/sprint0_arch.png)

## Piano di test

Questa prima fase di test seve ad effettuare un collaudo interno che in questa prima fase ha il preciso compito di confermare il corretto funzionamento della rete e delle interazioni via messaggi attraverso di essa dei vari componenti.

Il primo test ha l'obiettivo di confermare la ricezione dei messaggi da parte degli attori qak e la corretta formazione dei messaggi.
```text
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
```

Il secondo test ha l'obiettivo di confermare il corretto funzionamento del sistema in caso di arrivo concorrente di due richieste di carico
```text
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
```

## Piano di lavoro

Oltre a questo sprint 0 iniziale, dedicato all'impostazione del progetto, nel nostro processo Scrum abbiamo previsto tre sprint operativi:

1. Sprint 1
    - cargoservice (core business del sistema)
    - cargorobot
1. Sprint 2
    - sonar
    - hold
    - IO port e pushbutton
1. Sprint 3
    - led
    - web-gui
    - display

| Numero sprint             | Data inizio (indicativa)  | Data fine (indicativa)    | Lavoro Stimato Totale (h) |
|---------------------------|---------------------------|---------------------------|---------------------------|
| Sprint 1                  | 03/08/2026                | 8/08/2026                | 30                        |
| Sprint 2                  | 10/08/2026                | 14/08/2026                | 20                        |
| Sprint 3                  | 17/08/2026                | 21/08/2026                | 20                        |

La pianificazione temporale costituisce un riferimento per il team.  
Sono comunque contemplate variazioni, nel limite del ragionevole, purché non compromettano il ritmo generale del lavoro.  
Eventuali modifiche significative potranno essere apportate solo in presenza di esigenze straordinarie, cambiamenti rilevanti durante il percorso progettuale, o situazioni tali da non poter essere ignorate.  


## Team di lavoro e attività specifiche

Francesco Cenerini, Niccolò Leoncini, Pietro Milo Tasinato.
Tutti e tre i componenti del team hanno partecipato attivamente a tutte le fasi di scrittura e stesura dello sprint, dando il loro contributo in forma di conoscenze e ore di lavoro.
