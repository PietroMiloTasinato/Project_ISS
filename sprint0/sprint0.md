# Sprint0

## Indice

- [Sprint0](#sprint0)
  - [Indice](#indice)
  - [Obiettivi](#obiettivi)
  - [Requisiti](#requisiti)
  - [Vocabolario](#vocabolario)
  - [Macrocomponenti](#macrocomponenti)
  - [Architettura di riferimento](#architettura-di-riferimento)
  - [Piano di test](#piano-di-test)
  - [Piano di lavoro](#piano-di-lavoro)
  - [Team di lavoro e attività specifiche](#team-di-lavoro-e-attività-specifiche)

## Obiettivi

Lo sprint0 ha lo scopo di formalizzare i requisiti del committente riguardo al documento ["TemaFinale26"](https://anatali.github.io/issLab2026/_static/docs/Protobook.pdf#chapter.31) e definire una architettura iniziale del sistema che verrà implementata ed espansa negli sprint successivi.

## Requisiti

I requisiti sono descritti dal committente nel seguente documento: [TF2026 Requirements](https://anatali.github.io/issLab2026/_static/docs/Protobook.pdf#section.31.1).

## Vocabolario

- **Maritime Cargo Shipping Company**: rappresenta la compagnia marittima committente del sistema, d'ora in poi chiamata **Company**.
- **Differential Drive Robot**: è un robot virtuale, d'ora in poi chiamato **Cargorobot**, abilitato al movimento all'interno dell'area di hold e che si occupa di spostare i container.
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

## Architettura di riferimento

## Piano di test

## Piano di lavoro

## Team di lavoro e attività specifiche
