# Sprint2

## Indice

- [Goal dello Sprint](#goal-dello-sprint)
- [Analisi dei requisiti](#analisi-dei-requisiti)
- [Analisi del Problema](#analisi-del-problema)
- [Piano di Test](#piano-di-test)
- [Project](#project)
- [Testing](#testing)
- [Deployment](#deployment)
- [Piano di lavoro](#piano-di-lavoro)
- [Team di lavoro](#team-di-lavoro)

## Goal dello Sprint

Lo [Sprint1](../UserDocs/sprint1.md) ha consegnato il prototipo eseguibile del core business: il `cargoservice` valuta le richieste di carico, riserva lo slot nella `hold` e coordina il `cargorobot` lungo il percorso `IOPort → slot5 → slot riservato → home`. I dispositivi che interagiscono con il mondo esterno (IOPort, sonar, led) vi compaiono però solo come **attori mock** che rispettano un vocabolario di messaggi provvisorio, e due requisiti sono rimasti in sospeso: il display con lo stato della hold (R8) e lo stato _Out of service_ (R9).

> **Sostituzione dei mock con i dispositivi reali**: l'IOPort diventa una **Web GUI** (pushbutton e display), mentre sonar e led sono realizzati sul **Raspberry Pi Pico W**, come indicato dal committente nello [Sprint0](../UserDocs/sprint0.md). Lo Sprint2 analizza i dispositivi e le loro interazioni con il `cargoservice`, rivede esplicitamente il contratto di messaggi usato dai mock e chiude i requisiti R8 e R9, superando i TestPlan definiti prima dello sviluppo.

Nello Sprint1 dei dispositivi era entrato solo il contratto di interazione con il `cargoservice`; qui entrano l'analisi dei dispositivi veri e propri (misura di `D`, tempi, lampeggiamento del led, web-gui) e l'eventuale revisione di quel contratto. Il core business non è oggetto di nuovo sviluppo: viene modificato solo dove l'integrazione dei dispositivi lo richiede, e ogni modifica è motivata nell'analisi del problema.

## Analisi dei requisiti

### Requisiti del sottoinsieme

I requisiti sono numerati R1–R9 come nello [Sprint1](../UserDocs/sprint1.md). La tabella riassume che cosa lo Sprint1 ha già realizzato e che cosa resta allo Sprint2.

| Req. | Contenuto                                                                              | Stato dopo lo Sprint1                       | Contributo dello Sprint2                           |
| ---- | -------------------------------------------------------------------------------------- | ------------------------------------------- | -------------------------------------------------- |
| R1   | Richiesta di carico tramite il pushbutton dell'IOPort                                  | Simulata da `io_port` (`press_button`)      | Pushbutton reale nella Web GUI                     |
| R2   | `retrylater` se IOPort occupata o sistema _Out of service_                             | Solo IOPort occupata                        | Ramo _Out of service_ raggiungibile                |
| R3   | Rifiuto se la hold è piena                                                             | Realizzato                                  | Nessuno (la risposta è mostrata nel display)       |
| R4   | Accettazione con slot riservato                                                        | Realizzato                                  | Nessuno (la risposta è mostrata nel display)       |
| R5   | Led lampeggiante durante lo stato _engaged_                                            | `led_mock` (solo log)                       | Led del Pico W                                     |
| R6   | Container nell'area del sonar entro il tempo prefissato; rilevamento con `D < DFREE/2` | `sonar_mock` (risposta immediata o timeout) | Sonar reale (HC-SR04 sul Pico W)                   |
| R7   | Trasporto con il cargorobot                                                            | Realizzato                                  | Nessuno                                            |
| R8   | Display: stato della hold, `Service working`, `Out of service`                         | Rinviato                                    | Display della Web GUI                              |
| R9   | `Out of service` se `D > DFREE` per almeno 3 secondi                                   | Rinviato                                    | Rilevamento a partire dalla misura reale del sonar |

### I dispositivi

Per ciascun dispositivo riproponiamo le domande che lo Sprint0 poneva per i macro-componenti (natura logica, nodo, tecnologia, responsabilità).

| Dispositivo | Natura logica            | Nodo                  | Tecnologia           | Responsabilità funzionale                                                                           |
| ----------- | ------------------------ | --------------------- | -------------------- | --------------------------------------------------------------------------------------------------- |
| Web GUI     | Interfaccia web (client) | Browser del cliente   | HTML/JavaScript      | Pushbutton per `request_to_load`; display con risposta, stato della hold e stato del servizio       |
| Sonar       | Dispositivo/servizio     | Pico W                | HC-SR04, MicroPython | Misura la distanza `D`; consente di rilevare il container (`D < DFREE/2`) e il guasto (`D > DFREE`) |
| Led         | Dispositivo              | Pico W (led embedded) | MicroPython          | Lampeggia mentre il sistema è _engaged_                                                             |

**Sonar:** Secondo i requisiti il sonar misura una distanza `D` e il suo significato dipende dall'intervallo in cui `D` cade:

- `D < DFREE/2` per un tempo ragionevole (per esempio 3 secondi): container presente nell'area del sensore;
- `D > DFREE` per almeno 3 secondi: _Out of service_ (possibile guasto del sonar);
- `DFREE/2 ≤ D ≤ DFREE`: il testo non dice nulla.

**Led:** Il LED embedded del Pico W lampeggia per tutta la durata dello stato _engaged_, cioè dall'accettazione della richiesta fino al ritorno in _disengaged_.

**Web GUI:** Come stabilito nello Sprint0, per evitare ambiguità con la zona `IOPort` della hold (dove il cargorobot ritira il container) chiamiamo _web-gui_ il componente con cui interagisce il cliente. Essa mostra:

- la **risposta** alla richiesta (slot riservato, `retrylater` o rifiuto);
- lo **stato corrente della hold**;
- lo **stato del servizio**: `Service working` oppure `Out of service`.

### Assunzioni e domande al committente

La numerazione prosegue quella dello Sprint1 (Q1–Q7). Ogni punto è un'**assunzione da confermare**.

| #   | Domanda                                                                                                    | Assunzione provvisoria                                                                |
| --- | ---------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------- |
| Q8  | Quanto vale `DFREE`? Che cosa significa una misura con `DFREE/2 ≤ D ≤ DFREE`?                              | _TODO_: valore da concordare; la zona intermedia non cambia lo stato noto del sistema |
| Q9  | Lo stato _Out of service_ è reversibile? Quando `D` rientra nel campo, il servizio riparte?                | Sì: dopo una misura valida si torna a `Service working`                               |
| Q10 | Che cosa accade se il sonar va _Out of service_ mentre un carico è in corso?                               | _TODO_: il carico in corso prosegue e solo le nuove richieste ricevono `retrylater`?  |
| Q11 | Il display mostra lo stato di ciascuno slot (`FREE`, `BOOKED`, `OCCUPIED`) o solo quanti slot sono liberi? | Stato di ciascuno slot                                                                |
| Q12 | Sono previsti più client Web GUI contemporanei?                                                            | Sì, tutti ricevono gli stessi aggiornamenti del display                               |

## Analisi del Problema

### Il modello dell'analisi

_TODO: formalizzare il risultato dell'analisi in un modello Qak (`sprint2.qak`): vocabolario dei messaggi rivisto, Context e QActor._

Bozza delle **differenze** rispetto al vocabolario dello Sprint1. Ad esempio:

```text
// Sprint1 (mock)                              // Sprint2 (bozza)
Request detect_container(ARG)           -->    eventi dal sonar (presenza del container, stato del servizio)
Reply   container_detected / sonar_timeout     (timeout di 30 s gestito dal cargoservice)
Dispatch start_blinking / stop_blinking -->    invariati
Dispatch press_button(ARG)              -->    la request_to_load nasce dalla Web GUI
                                               nuovi eventi per il display: stato della hold, stato del servizio
```

## Piano di Test

_TODO: Bozza dei casi possibili da coprire:_

| Condizione iniziale / stimolo                                    | Risultato atteso                                                      | Req.   |
| ---------------------------------------------------------------- | --------------------------------------------------------------------- | ------ |
| Pressione del pushbutton della Web GUI, sistema a riposo         | La GUI mostra lo slot riservato                                       | R1, R4 |
| Pressione del pushbutton con hold piena                          | La GUI mostra il rifiuto                                              | R3     |
| Misura `D < DFREE/2` mantenuta per circa 3 s dopo l'accettazione | Container rilevato: il robot inizia il trasporto                      | R6     |
| Nessun container entro 30 s dall'accettazione                    | Il sistema torna _disengaged_; lo slot torna libero; il led si spegne | R6     |
| Misura `D > DFREE` per almeno 3 s                                | Display: `Out of service`; nuove richieste ricevono `retrylater`      | R2, R9 |
| Misura `D > DFREE` per meno di 3 s                               | Nessun cambio di stato                                                | R9     |
| Ritorno di `D` nel campo valido dopo _Out of service_            | Display: `Service working` (assunzione Q9)                            | R8, R9 |
| Stato _engaged_                                                  | Il led lampeggia; si spegne al ritorno in _disengaged_                | R5     |
| Modifica dello stato della hold                                  | Il display mostra lo stato aggiornato degli slot                      | R8     |

_//TODO: i casi relativi al Pico W sono verificabili senza hardware con il simulatore descritto nell'analisi (si veda "Strategia di sviluppo"); quelli che richiedono il dispositivo vanno distinti, come nello Sprint1, dai test eseguibili in automatico._

## Project

_TODO: specializzazione del modello dell'analisi in `sprint2.qak` e codice dei dispositivi._

Punti da sviluppare:

- vocabolario dei messaggi definitivo;
- modifiche al `cargoservice` (gestione di `System_out_of_service`, timeout di consegna, nuovi eventi);
- attore di collegamento con il Pico W;
- programma MicroPython del Pico W (sonar HC-SR04 e led);
- Web GUI (`webgui.html`, `websocket.js`) e web server;
- simulatore del Pico W per lo sviluppo senza hardware. (?)

## Testing

_TODO: da completare dopo il Project._

Punti da sviluppare:

- classe di test dello Sprint2 e rapporto con `Sprint1Test` (che continua a valere per il core business);
- test con misure simulate del sonar;
- test con il Pico W reale (dopo la consegna);
- esiti dei test (tabella requisiti/esito, come nello Sprint1).

## Deployment

_TODO: da completare dopo il Project._

Punti da sviluppare:

- ambiente del robot (`robotsmart26.yaml`), come nello Sprint1;
- Context `ctx_cargoservice` e web server (la porta 8010 del web server è quella dello scheletro del progetto);
- preparazione del Pico W: firmware MicroPython e caricamento del programma (Protobook, capitolo _PicoW_);
- Container Docker.

## Piano di lavoro

## Team di lavoro

**Francesco Cenerini, Niccolò Leoncini, Pietro Milo Tasinato**.
