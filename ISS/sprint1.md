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

![Wenv & DDR](/img/sprint0_arch.png)

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



![Wenv & DDR](/img/CargoBot.png)

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
