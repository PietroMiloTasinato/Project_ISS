# Sprint0

## Indice

- [Sprint0](#sprint0)
  - [Indice](#indice)
  - [Obiettivi](#obiettivi)
  - [Requisiti](#requisiti)
  - [Vocabolario](#vocabolario)
  - [Motivazione sull'utilizzo del metamodello Qak](#motivazione-sullutilizzo-del-metamodello-qak)
  - [Macrocomponenti](#macrocomponenti)
  - [Architettura di riferimento](#architettura-di-riferimento)
    - [Messaggi](#messaggi)
    - [Diagramma dell'architettura](#diagramma-dellarchitettura)
  - [Piano di test](#piano-di-test)
  - [Piano di lavoro](#piano-di-lavoro)
  - [Team di lavoro e attività specifiche](#team-di-lavoro-e-attività-specifiche)

## Obiettivi

Lo sprint0 ha lo scopo di formalizzare i requisiti del committente riguardo al documento ["TemaFinale26"](https://anatali.github.io/issLab2026/_static/docs/Protobook.pdf#chapter.31) e definire una architettura iniziale del sistema che verrà implementata ed espansa negli sprint successivi.

## Requisiti

I requisiti sono descritti dal committente nel seguente documento: [TF2026 Requirements](https://anatali.github.io/issLab2026/_static/docs/Protobook.pdf#section.31.1).

## Vocabolario

- **Maritime Cargo Shipping Company**: rappresenta la compagnia marittima committente del sistema, d'ora in poi chiamata **Company**.
- **Differential Drive Robot (DDR)**: è un robot virtuale, d'ora in poi chiamato **Cargorobot**, abilitato al movimento all'interno dell'area di hold e che si occupa di spostare i container.
- **Stiva**: area rettangolare che definisce lo spazio di movimento del robot in cui vengono caricati i container, chiamata d'ora in poi **Hold**. Contiene gli slot1-4, lo slot5, l'IOPort, il sonar e il robot.
- **Slot1-4**: aree per lo stoccaggio dei container.
- **Slot5**: area temporanea adibita per la fase di marcatura prima dello stoccaggio finale in uno degli altri slot.
- **IOPort**: zona di ingresso dei container facente parte della Hold.
- **Sensor**: sensore associato all'IOPort, usato per rilevare la presenza di un container se misura una distanza $D < DFREE/2$ durante un intervallo di tempo (es. 3 sec).
- **Led**: dispositivo che deve lampeggiare quando il sistema è occupato (_engaged_), cioè quando una richiesta di carico è stata accettata e il sistema sta gestendo tale richiesta.
- **Container**: è un oggetto di dimensioni predefinite che può essere trasportato dal Cargorobot e successivamente etichettato con un codice a barre da un dispositivo marker situato nello slot5.

## Motivazione sull'utilizzo del metamodello Qak

In questo progetto abbiamo scelto di utilizzare il metamodello **Quasi Actor Kotlin** (Qak) per 2 vantaggi principali offerti da questo metamodello rispetto ad altri General Purpose Language (GPL):

- Il concetto di **Interconnessione** è supportato nativamente in Qak: nel dominio descritto del problema possiamo notare che la comunicazione tra i vari attori presenti è essenziale per il corretto funzionamento del sistema, e un solo GPL non permette di esprimere questa necessità nativamente.

- Progettazione orientata ad attori e microservizi: in un GPL saremmo limitati vedere gli elementi del sistema come dei semplici POJO. Qak invece permette di esprimere gli elementi del sistema come attori, supportando l'**Actor Model** (il paradigma di progettazione ad attori), questo ci permette di sviluppare il nostro progetto attorno alle interaizoni tra attori, e non sulla semplice regolazione di uno o più flussi di dati.

Queste due caratteristiche permettono di ridurre significativamente l'**Abstraction Gap** tra Qak e il dominio del problema. Utilizzare solamente un GPL presenterebbe un Abstraction gap maggiore che comporterebbe a maggiore complessità nelle fasi di implementazione del progetto.

Infine, l'adozione del metamodello QAK è anche motivata dalla presenza di una **Software Factory** che è in grado di generare automaticamente codice Kotlin a partire dai modelli scritti in linguaggio QAK. Questo permette non solo di ottenere dei modelli eseguibili "off-the-bat", ma supporta anche un processo di **Rapida Prototipazione** in cui si permette agli sviluppatori di valutare la fattibilità di particolari soluzioni implementative già nelle fasi di progettazione.

## Macrocomponenti

Si riportano i macrocomponenti software del sistema che sono già forniti dal committente: **VirtualRobot** e **WEnv**. In particolare, il WEnv è un'ambiente virtuale che include un simulatore di un _Differential Drive robot_ (DDR), un particolare robot virtuale che possiede due ruote motrici sullo stesso asse e una terza ruota condotta (non motrice) in cui le possibili mosse sono:

- muoversi avanti-indietro lungo una direzione costante;
- fermarsi;
- ruotare di 90° a destra o sinistra.

Queste mosse sono realizzate inviando opportuni comandi al robot simulato.
Il WEnv rappresenta la stiva della nave in cui il cargorobot dovrà muoversi e il VirtualRobot è un componente software che permette di controllare un DDR virtuale all'interno di WEnv.

Per quanto riguarda, i macrocomponenti da sviluppare riportiamo:

- **cargoservice**
- **cargorobot**
- **sonar**
- **IOPort**: composto da un _display_ (web-gui) + _pushbutton_
- **hold** <!-- cosa si intende per stato corrente dell'hold -->
- **led** <!-- chiedere se va tenuto come componente separato o inserirlo dentro IOPort -->

## Architettura di riferimento

Dato che adottiamo il paradigma ad attori di qak, ogni entità ha un proprio flusso di dati indipendente perciò la comunicazione deve avvenire tramite messaggi perchè non è possibile la condivisione di memoria.

### Messaggi

Si definiscono solamente i messaggi che modellano le interazioni espresse esplicitamente dal committente nel documento dei requisiti:

```
Request load_container : load_container(ARG)
Reply load_accepted : load_accepted(SLOT) for load_container
Reply load_refused : load_refused(CAUSE) for load_container
Reply retrylater : retrylater(CAUSE) for load_container

Dispatch move_container_to_slot : move_container_to_slot(SLOT)
Event container_marked : container_marked()
Event container_detected : container_detected(ARG)

Event sonar_failure : sonar_failure(ARG)

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
- ctx_client: client.

## Piano di test

## Piano di lavoro

## Team di lavoro e attività specifiche

Francesco Cenerini, Niccolò Leoncini, Pietro Milo Tasinato.
Tutti e tre i componenti del team hanno partecipato attivamente a tutte le fasi del progresso dello sprint, dando il loro contributo sia in forma di conoscenze, che di ore di lavoro.
