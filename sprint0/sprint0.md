# Sprint0

## Indice

- [Sprint0](#sprint0)
  - [Indice](#indice)
  - [Obiettivi](#obiettivi)
  - [Requisiti](#requisiti)
    - [Requisiti funzionali individuati](#requisiti-funzionali-individuati)
  - [Motivazione sull'utilizzo del metamodello Qak](#motivazione-sullutilizzo-del-metamodello-qak)
  - [Macrocomponenti](#macrocomponenti)
    - [Infrastruttura fornita](#infrastruttura-fornita)
    - [Componenti software da sviluppare](#componenti-software-da-sviluppare)
  - [Core business](#core-business)
  - [Architettura di riferimento](#architettura-di-riferimento)
    - [Messaggi](#messaggi)
    - [Diagramma dell'architettura](#diagramma-dellarchitettura)
  - [Piano di test](#piano-di-test)
  - [Piano di lavoro](#piano-di-lavoro)
  - [Team di lavoro e attività specifiche](#team-di-lavoro-e-attività-specifiche)

## Obiettivi

Lo Sprint0 ha lo scopo di formalizzare i requisiti espressi dal committente nel documento [TemaFinale26](https://anatali.github.io/issLab2026/_static/docs/Protobook.pdf#chapter.31) e di definire un primo modello del sistema da utilizzare come riferimento negli sprint successivi.

## Requisiti

I requisiti sono descritti dal committente nel documento [TF2026 Requirements](https://anatali.github.io/issLab2026/_static/docs/Protobook.pdf#section.31.1).

### Requisiti funzionali individuati

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
- **RF14 - Guasto del sonar**: se il sonar misura una distanza `D > DFREE` per almeno 3 secondi, il sistema deve passare allo stato `out_of_service` e il display deve mostrare `Out of service`.

## Motivazione sull'utilizzo del metamodello Qak

In questo progetto abbiamo scelto di utilizzare il metamodello **Quasi Actor Kotlin** (Qak) per 2 vantaggi principali offerti da questo metamodello rispetto ad altri General Purpose Language (GPL):

- Il concetto di **Interconnessione** è supportato nativamente in Qak: nel dominio descritto del problema possiamo notare che la comunicazione tra i vari attori presenti è essenziale per il corretto funzionamento del sistema, e un solo GPL non permette di esprimere questa necessità nativamente.

- Progettazione orientata ad attori e microservizi: in un GPL saremmo limitati vedere gli elementi del sistema come dei semplici POJO. Qak invece permette di esprimere gli elementi del sistema come attori, supportando l'**Actor Model** (il paradigma di progettazione ad attori), questo ci permette di sviluppare il nostro progetto attorno alle interaizoni tra attori, e non sulla semplice regolazione di uno o più flussi di dati.

Queste due caratteristiche permettono di ridurre significativamente l'**Abstraction Gap** tra Qak e il dominio del problema. Utilizzare solamente un GPL presenterebbe un Abstraction gap maggiore che comporterebbe a maggiore complessità nelle fasi di implementazione del progetto.

Infine, l'adozione del metamodello QAK è anche motivata dalla presenza di una **Software Factory** che è in grado di generare automaticamente codice Kotlin a partire dai modelli scritti in linguaggio QAK. Questo permette non solo di ottenere dei modelli eseguibili "off-the-bat", ma supporta anche un processo di **Rapida Prototipazione** in cui si permette agli sviluppatori di valutare la fattibilità di particolari soluzioni implementative già nelle fasi di progettazione.

## Macrocomponenti

L'analisi distingue le entità fisiche del dominio dai componenti software utilizzati per controllarle o rappresentarle.

### Infrastruttura fornita

Si riportano i macrocomponenti software del sistema che sono già forniti dal committente:

- **WEnv**: ambiente virtuale che simula la hold.
- **VirtualRobot**: componente software che permette di controllare un Differential Drive Robot all'interno di WEnv.

Il DDR supporta le seguenti mosse elementari:

- movimento in avanti;
- movimento all'indietro;
- arresto;
- rotazione di 90° a destra;
- rotazione di 90° a sinistra.

![Wenv & DDR](/img/CargoBot.png)

### Componenti software da sviluppare

Per quanto riguarda, i macrocomponenti da sviluppare riportiamo:

- **cargoservice**
- **cargorobot**
- **sonar**
- **IOPort**: composto da un _display_ (web-gui) + _pushbutton_
- **hold** <!-- cosa si intende per stato corrente dell'hold -->
- **led** <!-- chiedere se va tenuto come componente separato o inserirlo dentro IOPort -->

## Core business

Il core business del sistema è la gestione coordinata del processo di caricamento di un container, dalla richiesta iniziale fino al suo deposito nello slot finale. Il processo principale è costituito dalle seguenti fasi:

1. il cliente preme il pushbutton della IOPort;
2. l'IOPort inoltra la richiesta al cargoservice;
3. il cargoservice verifica lo stato del servizio;
4. il cargoservice verifica che l'IOPort non sia occupato o che il sistema non sia fuori servizio;
5. il cargoservice richiede la prenotazione di uno slot;
6. se non è disponibile alcuno slot, la richiesta viene rifiutata;
7. in caso contrario, il sistema entra nello stato `engaged`;
8. il LED inizia a lampeggiare e il nome dello slot riservato viene comunicato al cliente;
9. il sistema attende il rilevamento del container entro il timeout previsto;
10. se il timeout scade, la prenotazione viene annullata, il LED viene spento e il sistema torna `disengaged`;
11. se il container viene rilevato nella sensor area, il cargoservice ordina al cargorobot di trasportarlo dall'IOPort allo slot5;
12. una volta che il container è arrivato in prossimità dello slot5, inizia la fase di marcatura e il sistema ne attende il completamento;
13. completata la fase di marcatura, il cargoservice ordina al cargorobot di trasportare il container dallo slot5 allo slot riservato;
14. il display viene aggiornato con lo stato corrente della hold;
15. il LED viene spento e il sistema torna `disengaged`.

La lettura delle misure del sonar, il controllo delle mosse elementari del robot e il rendering del display sono funzionalità di supporto. Il coordinamento della procedura descritta costituisce invece la logica applicativa centrale.

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

![Wenv & DDR](/img/IDSS_Scheme.drawio.png)

## Piano di test

Questa prima fase di test seve ad effettuare un collaudo interno che in questa prima fase ha il preciso compito di confermare il corretto funzionamento della rete e delle interazioni via messaggi attraverso di essa dei vari componenti.

## Piano di lavoro

## Team di lavoro e attività specifiche

Francesco Cenerini, Niccolò Leoncini, Pietro Milo Tasinato.
Tutti e tre i componenti del team hanno partecipato attivamente a tutte le fasi di scrittura e stesura dello sprint, dando il loro contributo in forma di conoscenze e ore di lavoro.
