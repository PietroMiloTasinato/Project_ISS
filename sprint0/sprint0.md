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

## Macrocomponenti

## Architettura di riferimento

## Piano di test

## Piano di lavoro

## Team di lavoro e attività specifiche
