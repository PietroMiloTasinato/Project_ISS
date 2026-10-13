# Sprint0

## Indice

- [Introduzione](#introduzione)
- [Requisiti](#requisiti)
- [Analisi dei Requisiti](#analisi-dei-requisiti)
- [Analisi del problema](#analisi-del-problema)
  - [Core business](#core-business)
  - [Infrastruttura fornita](#infrastruttura-fornita)
  - [Motivazione sull'utilizzo del metamodello Qak](#motivazione-sullutilizzo-del-metamodello-qak)
  - [Architettura di riferimento](#architettura-di-riferimento)
- [Piano di Test](#piano-di-test)
- [Project](#project)
  - [Diagramma dell'architettura iniziale](#diagramma-dellarchitettura-iniziale)
- [Testing](#testing)
- [Deployment](#deployment)
- [Piano di lavoro e mantenimento](#piano-di-lavoro-e-mantenimento)
- [Team di lavoro e attività specifiche](#team-di-lavoro-e-attività-specifiche)

## Introduzione

L'obiettivo dello Sprint0 è formalizzare quanto più possibile i requisiti forniti dal committente nel documento [TemaFinale26](https://anatali.github.io/issLab2026/_static/docs/Protobook.pdf#chapter.31), individuare le macro-parti del sistema e la loro natura software, riconoscere il core business, predisporre un primo insieme di TestPlan funzionali e definire un primo modello del sistema da utilizzare come riferimento negli sprint successivi.

In particolare, il primo modello dei requisiti prodotto deve essere comprensibile alla macchina dove possibile, senza introdurre componenti, messaggi o comportamenti che non emergono dai requisiti.

## Requisiti

I requisiti sono descritti dal committente nel documento [TF2026 Requirements](https://anatali.github.io/issLab2026/_static/docs/Protobook.pdf#section.31.1).Per comodità, riportiamo di seguito il suddetto documento, tradotto in lingua italiana:

Una compagnia di trasporto marittimo merci, da ora in poi semplicemente **company**, intende automatizzare le operazioni di carico dei **container** nella stiva della nave, da ora in poi semplicemente **hold**. A tale scopo, la compagnia intende impiegare un robot a guida differenziale, da ora in poi chiamato **cargorobot**.

La hold è un’area rettangolare e piana dotata di una porta di Input/Output, chiamata **IOPort**. L’area fornisce `4 slot` per immagazzinare i container e uno slot chiamato **slot5**.

![Wenv & DDR](../img/CargoBot.png)

Nella figura sopra:

- I **slots1-4** rappresentano le aree della stiva riservate a contenere un _container_ ciascuna.
- Lo **slot5** rappresenta un’area in cui il _cargorobot_ deve depositare temporaneamente un container prima di collocarlo in uno degli _slots1-4_. Durante questa sosta temporanea, un dispositivo chiamato marker etichetta il container con un codice a barre identificativo e segnala quando l’attività di marcatura è completata.
- L’**IOPort** è un dispositivo dotato di un **pushbutton** e di un **display**. Il _pushbutton_ viene premuto dal cliente per inviare una richiesta di carico di un container nella stiva. Il _display_ viene usato per mostrare la risposta alla richiesta e per mostrare lo stato corrente dell'_hold_.
- Il **sensore** associato all’_IOPort_ è un dispositivo, cioè un _sonar_, usato per rilevare la presenza di un container quando misura una distanza D tale che D < DFREE / 2 per un tempo ragionevole, per esempio 3 secondi.

La compagnia ci chiede di costruire un servizio chiamato **cargoservice**, che deve funzionare come segue. Il _cargoservice_ è in grado di ricevere una **richiesta di carico** di un container inviata da un cliente tramite il _pushbutton_ dell’_IOPort_.

Il servizio:

- Invia la risposta `retrylater` se l’**IOPort** è attualmente occupato da un container oppure se il sistema è _Out of service_.
- Rifiuta la richiesta quando la stiva è già piena, cioè quando gli `slots1-4` sono già occupati.
- Altrimenti, considera il sistema come **engaged**, individua uno slot libero e restituisce come risposta il nome dello slot riservato. Mentre il sistema è engaged, deve lampeggiare un LED.

Quando la _richiesta di carico_ viene accettata, il cliente deve spostare il container nell’area del _sensore_ entro un tempo prefissato, per esempio 30 secondi; altrimenti il sistema torna nello stato **disengaged**. Successivamente, il _cargoservice_ usa il _cargorobot_ per spostare il container dall’_IOPort_ allo _slot5_, dove il container viene marcato, e poi allo slot riservato.

Il servizio deve inoltre mostrare sul _display_ dell’_IOPort_:

- lo stato corrente dell'_hold_;
- il messaggio **'Service working'**, quando tutto procede correttamente;
- il messaggio **'Out of service'** se il _sensore sonar_ misura una distanza D > DFREE per almeno 3 secondi, evento che potrebbe indicare un guasto del _sonar_.

<!-- ### Requisiti funzionali individuati

Dall'analisi del documento dei requisiti emergono i seguenti comportamenti osservabili:

- **RF1 - Richiesta di carico**: il cliente invia una richiesta di carico mediante il pushbutton dell'IOPort.
- **RF2 - IOPort occupato**: se l'IOPort è già occupato da un container, il sistema risponde `retrylater`.
- **RF3 - Servizio non disponibile**: se il sistema si trova nello stato `out_of_service`, il sistema risponde `retrylater`.
- **RF4 - Hold piena**: se gli slot1-4 sono tutti occupati, la richiesta viene rifiutata.
- **RF5 - Accettazione della richiesta**: se il sistema è operativo, l'IOPort è libero ed esiste almeno uno slot disponibile, il sistema riserva uno slot e restituisce al cliente il nome dello slot riservato.
- **RF6 - Stato engaged**: dopo l'accettazione della richiesta il sistema entra nello stato `engaged` e il LED deve lampeggiare.
- **RF7 - Timeout di deposito**: il cliente deve collocare il container nell'area del sensore entro un intervallo di tempo prefissato, indicativamente 30 secondi. In caso contrario, il sistema torna `disengaged` e lo slot riservato viene liberato.
- **RF8 - Rilevamento del container**: il sonar rileva la presenza del container quando misura una distanza `D < DFREE/2` per un intervallo ragionevole, indicativamente 3 secondi.
- **RF9 - Trasporto verso slot5**: dopo il rilevamento, il cargorobot trasporta il container dall'IOPort allo slot5.
- **RF10 - Marcatura**: nello slot5 il marker assegna al container un codice a barre e segnala il completamento della marcatura.
- **RF11 - Trasporto finale**: dopo il completamento della marcatura, il cargorobot trasporta il container dallo slot5 allo slot precedentemente riservato.
- **RF12 - Aggiornamento della hold**: al termine del trasporto, lo slot riservato diventa occupato e il nuovo stato della hold viene mostrato sul display.
- **RF13 - Stato del servizio**: il display deve mostrare il messaggio `Service working` quando il sistema opera correttamente.
- **RF14 - Guasto del sonar**: se il sonar misura una distanza `D > DFREE` per almeno 3 secondi, il sistema deve passare allo stato `out_of_service` e il display deve mostrare `Out of service`. -->

## Analisi dei Requisiti

<!--In questa fase dobbiamo formalizzare i requisiti espressi in linguaggio umano, traducendoli in un linguaggio che sia comprensibile ad una macchina.
Innanzitutto dobbiamo trovare il linguaggio che più si adatta al sistema che dobbiamo implementare. In particolare, il committente richiede un sistema eterogeneo, dato che dobbiamo integrare almeno 3 dispositivi (computer dove esguire *cargoservice*, sonar e un dispositivo che gestisca l'*IOPort*)
-->

Seguendo i requisiti del committente, abbiamo individuato i seguenti **macrocomponenti**:

- **cargoservice**
- **cargorobot**
- **sonar**
- **IOPort**
- **hold**
- **led**
- **slot** numerati dall' 1 al 4
- **slot 5**, d'ora in poi identificato con **marker**
- **container**

<!-- `cargoservice` è composto da un servizio che si occupa di ricevere la richiesta di carico dal cliente. Successivamente deve ricevere e gestire le possibili risposte:

```
- Request accepted
- Request refused
- Riprova più tardi
``` -->

Il sistema richiesto dal committente ha l’obiettivo di automatizzare le operazioni di caricamento e deposito dei container all’interno della stiva di una nave, mediante l’impiego di un robot a guida differenziale denominato `cargorobot`, coordinato dal servizio software `cargoservice`.
L'`hold` (stiva) costituisce l’ambiente fisico operativo ed è organizzata in quattro aree di deposito permanente, denominate `slot1–slot4`, ciascuna destinata a ospitare un solo container, e in un’area di deposito temporaneo, `slot5`, dedicata alle operazioni di marcatura. L’interazione con il sistema avviene attraverso la `IOPort`, interfaccia verso il cliente, dotata di un pulsante (_pushbutton_) per inoltrare le richieste di caricamento e di un _display_ per visualizzare le risposte, lo stato corrente della stiva e le informazioni sul funzionamento del servizio.
Alla IOPort è associato un sensore `sonar`, incaricato di rilevare la presenza di un container nell’area di ingresso e di segnalare eventuali condizioni anomale della distanza misurata.

Il `cargoservice` rappresenta il servizio applicativo responsabile della ricezione e della valutazione delle richieste di caricamento, nonché del coordinamento delle operazioni necessarie al loro completamento. In particolare, il servizio restituisce la risposta _retrylater_ qualora la IOPort risulti occupata da un container oppure il sistema si trovi nella condizione _Out of service_; rifiuta invece la richiesta quando tutti gli slot di deposito permanente risultano occupati. In presenza di almeno uno slot disponibile e in assenza delle precedenti condizioni, il servizio accetta la richiesta, individua e riserva uno slot libero, ne comunica il nome al cliente e pone il sistema nello stato _engaged_, durante il quale un `led` deve lampeggiare. A seguito dell'accettazione, il cliente è tenuto a collocare il container nell’area di rilevamento del sonar entro un intervallo di tempo prefissato, trascorso il quale, in assenza del container, il sistema torna nello stato _disengaged_.

Una volta rilevata la presenza del container, il cargoservice si avvale del cargorobot per trasferirlo dalla IOPort allo slot5, dove il dispositivo `marker` applica un codice a barre identificativo e segnala il completamento dell’operazione di marcatura. Successivamente, il robot provvede al trasferimento del container nello slot precedentemente riservato. Durante il funzionamento, la IOPort deve inoltre visualizzare lo stato corrente della stiva e il messaggio _Service working_ quando il sistema opera regolarmente, oppure _Out of service_ qualora il sonar rilevi, per almeno tre secondi, una distanza superiore a `DFREE`, condizione indicativa di una possibile anomalia del sensore.

Dall’analisi dei requisiti emerge un sistema caratterizzato dalla cooperazione di diverse entità software e dispositivi fisici, la cui distribuzione sui nodi computazionali non è esplicitamente definita dal committente. Sebbene il testo dei requisiti non imponga un’architettura distribuita, gli obiettivi del corso orientano verso la realizzazione di un sistema distribuito, potenzialmente eterogeneo, costituito da più nodi computazionali. Il numero di nodi computazionali ricavato dai requisiti è almeno 2 (sonar e un computer per eseguire `cargoservice`), in caso durante la fase di progettazione dovesse sorgere la necessità di usare altri nodi, essi dovrano essere definiti nelle successive fasi di analisi e progettazione. In questa fase iniziale è quindi possibile prevedere un primo modello del sistema organizzato in un unico Context, eventualmente mediante componenti mock, senza vincolare l’architettura definitiva. Analogamente, il committente non prescrive l’utilizzo di uno o più linguaggi di programmazione. Dopo un confronto con il commitente, sono sorti altri requisiti, in particolare: 
 - la presenza di una Web GUI per la IOPort
 -  un dispositivo PicoW per il sonar e il LED
Questi ulteriori requisiti introducono dei vincoli tecnologici che potrebbero richiedere l’impiego di tecnologie e linguaggi differenti (**da capire bene come introdurre questi ulteriori requisiti specificati nel file AboutSprint del prof**).

L'interazione principale è una richiesta dalla IOPort al cargoservice, seguita da una risposta. Un primo vocabolario Qak, con nomi proposti dagli analisti per gli esiti non nominati dal committente, è:

```
Request load_container : load_container(ARG)
Reply load_accepted : load_accepted(SLOT) for load_container
Reply load_refused : load_refused(CAUSE) for load_container
Reply retrylater : retrylater(CAUSE) for load_container
```

Il frammento descrive solo i messaggi: non è un modello Qak completo già validato o eseguibile. Inoltre, le interazioni rimanenti tramite messaggi verranno discusse e modellate durante le fasi di analisi nei successivi sprint.

## Analisi del problema

### Core business

Il core business consiste nel coordinamento del processo di caricamento, dalla richiesta all'eventuale deposito nello slot riservato. Il servizio deve consentire l'accettazione solo nelle condizioni indicate, riservare lo slot e organizzare le operazioni: posizionamento del container, trasporto allo `slot5`, marcatura e trasporto finale. Le operazioni sensoriali, l'attuazione dei movimenti e la visualizzazione sono funzionalità di supporto al processo.

```
Cliente → IOPort → cargoservice → esito della richiesta
                                      │
                        [richiesta accettata]
                                      ↓
                      slot riservato; sistema engaged
                                      ↓
                         attesa del container
                           │           │
                         timeout    rilevamento
                           ↓           ↓
                      disengaged    cargorobot
                                       ↓
                                     slot5
                                       ↓
                                   marcatura
                                       ↓
                                 slot riservato
```

Questo è un flusso funzionale, non un diagramma di attori Qak o di deployment. Le transizioni del LED e il rilascio della prenotazione dopo il timeout non vengono completati arbitrariamente laddove il testo non li specifica.

<!-- Il core business del sistema è la gestione coordinata del processo di carico di un container, dalla richiesta iniziale fino al suo deposito nello slot finale. Il processo principale è costituito dalle seguenti fasi:

1. il cliente preme il pushbutton della IOPort;
2. l'IOPort inoltra la richiesta al cargoservice_handler;
3. il cargoservice_handler verifica lo stato del sistema;
4. il cargoservice_handler verifica che l'IOPort non sia occupato o che il sistema non sia fuori servizio;
5. il cargoservice_worker richiede la prenotazione di uno slot;
6. se non è disponibile alcuno slot, la richiesta viene rifiutata;
7. in caso contrario, il sistema entra nello stato `engaged`;
8. il LED inizia a lampeggiare e il nome dello slot riservato viene comunicato al cliente;
9. il sistema attende il rilevamento del container entro il timeout previsto;
10. se il timeout scade, la prenotazione viene annullata, il LED viene spento e il sistema torna `disengaged`;
11. se il container viene rilevato nella sensor area, il cargoservice_worker ordina al cargorobot di trasportarlo dall'IOPort allo slot5;
12. una volta che il container è arrivato in prossimità dello slot5, inizia la fase di marcatura e il sistema ne attende il completamento;
13. completata la fase di marcatura, il cargoservice_worker ordina al cargorobot di trasportare il container dallo slot5 allo slot riservato;
14. il display viene aggiornato con lo stato corrente della hold;
15. il LED viene spento e il sistema torna allo stato `disengaged`.

La lettura delle misure del sonar, il controllo delle mosse elementari del robot e il rendering del display sono funzionalità di supporto. Il coordinamento della procedura descritta costituisce invece la logica applicativa centrale. -->

### Infrastruttura fornita

Si riportano i macrocomponenti software del sistema che sono già forniti dal committente come `WEnv` che rappresenta un'ambiente virtuale che simula la hold e diversi componenti per il controllo robotico: `RobotObj26` (POJO), `VirtualRobot26`, `VirtuRobotService26` e `RobotSmart26`. L'analisi successiva dovrà confrontare capacità, interfacce e costi d'integrazione di questi strumenti per stabilire quale supporto utilizzare nel `cargorobot`. Non è richiesto di riscrivere da zero tutte le funzionalità robotiche.

### Motivazione sull'utilizzo del metamodello Qak

La scelta del metamodello **Quasi Actor Kotlin** (Qak) è motivata da due vantaggi principali offerti da questo metamodello rispetto ad altri General Purpose Language (GPL):

- Il concetto di **Interconnessione** è supportato nativamente in Qak: nel dominio descritto del problema possiamo notare che la comunicazione tra i vari attori presenti è essenziale per il corretto funzionamento del sistema, e un solo GPL non permette di esprimere questa necessità nativamente.

- Progettazione orientata ad attori e microservizi: in un GPL saremmo limitati vedere gli elementi del sistema come dei semplici POJO. Qak invece permette di esprimere gli elementi del sistema come attori, supportando l'**Actor Model** (il paradigma di progettazione ad attori), questo ci permette di sviluppare il nostro progetto attorno alle interaizoni tra attori, e non sulla semplice regolazione di uno o più flussi di dati.

Queste due caratteristiche permettono di ridurre significativamente l'**Abstraction Gap** tra Qak e il dominio del problema. Utilizzare solamente un GPL presenterebbe un Abstraction gap maggiore che comporterebbe a maggiore complessità nelle fasi di implementazione del progetto.

Infine, l'adozione del metamodello QAK è anche motivata dalla presenza di una **Software Factory** che è in grado di generare automaticamente codice Kotlin a partire dai modelli scritti in linguaggio QAK. Questo permette non solo di ottenere dei modelli eseguibili "off-the-bat", ma supporta anche un processo di **Rapida Prototipazione** in cui si permette agli sviluppatori di valutare la fattibilità di particolari soluzioni implementative già nelle fasi di progettazione.

### Architettura di riferimento

Nel modello qak, ogni attore possiede uno stato privato e un proprio comportamento. Gli attori non accedono direttamente allo stato interno degli altri attori, ma coordinano le proprie attività mediante lo scambio di messaggi.

L'assenza di memoria condivisa è considerata un vincolo del modello logico: anche quando più attori vengono eseguiti nella stessa JVM, il loro coordinamento deve avvenire tramite messaggi e non mediante accesso diretto a variabili mutabili condivise.

La motivazione di Qak non determina automaticamente il numero degli attori o dei Context e non obbliga a rappresentare ogni dispositivo fisico come QActor.

## Piano di Test

Primi casi funzionali da formalizzare in test automatici quando sarà disponibile il prototipo:

| Condizione iniziale e richiesta                     | Risultato atteso                       |
| --------------------------------------------------- | -------------------------------------- |
| IOPort libera, servizio operativo, slot disponibile | Risposta con nome dello slot riservato |
| IOPort occupata da un container                     | `retrylater`                           |
| Servizio Out of service                             | `retrylater`                           |
| Hold piena, IOPort libera e servizio operativo      | Richiesta rifiutata                    |

Non sono test già eseguiti. I casi relativi a timeout, trasporto e sonar saranno approfonditi negli sprint che li affronteranno.

## Project

### Diagramma dell'architettura iniziale

I requisiti permettono una vista logica delle macro-parti, ma non ancora una distribuzione sui nodi. Il seguente diagramma rappresenta l'architettura iniziale di riferimento per lo sprint 1 e le relazioni qui sotto mostrano chi interagisce funzionalmente con chi, senza imporre un protocollo concreto o un attore per ogni dispositivo.

**TODO**:

- ctx_cargoservice: cargoservice_handler, cargoservice_worker, hold, cargorobot, marker, sonar;
- ctx_ioport: ioport, display, pushbutton;
- ctx_devices: led;
- ctx_client: client. <!-- realizza il cliente -->

![Wenv & DDR](../img/sprint0_arch.png)

**Goal dello Sprint1**: realizzare un primo prototipo dell'interazione IOPort–cargoservice che gestisca accettazione, rifiuto e retrylater, utilizzando mock se necessari. Il trasporto effettivo e l'integrazione dei dispositivi sono rinviati ai successivi incrementi.

## Testing

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

## Deployment

Non previsto per lo Sprint0: non è ancora disponibile un prototipo da distribuire.

## Piano di lavoro e mantenimento

Oltre a questo sprint 0 iniziale, dedicato all'impostazione del progetto, nel nostro processo Scrum abbiamo previsto tre sprint operativi:

1. Sprint 1
   - cargoservice (core business del sistema)
   - cargorobot
   - marker
1. Sprint 2
   - sonar
   - hold
   - IO port e pushbutton
1. Sprint 3
   - led
   - web-gui
   - display

| Numero sprint | Data inizio (indicativa) | Data fine (indicativa) | Lavoro Stimato Totale (h) |
| ------------- | ------------------------ | ---------------------- | ------------------------- |
| Sprint 1      | 03/08/2026               | 8/08/2026              | 30                        |
| Sprint 2      | 10/08/2026               | 14/08/2026             | 20                        |
| Sprint 3      | 17/08/2026               | 21/08/2026             | 20                        |

La pianificazione temporale costituisce un riferimento per il team.  
Sono comunque contemplate variazioni, nel limite del ragionevole, purché non compromettano il ritmo generale del lavoro.  
Eventuali modifiche significative potranno essere apportate solo in presenza di esigenze straordinarie, cambiamenti rilevanti durante il percorso progettuale, o situazioni tali da non poter essere ignorate.

## Team di lavoro e attività specifiche

Francesco Cenerini, Niccolò Leoncini, Pietro Milo Tasinato.
Tutti e tre i componenti del team hanno partecipato attivamente a tutte le fasi di scrittura e stesura dello sprint, dando il loro contributo in forma di conoscenze e ore di lavoro.
