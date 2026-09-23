%====================================================================================
% sprint1 description   
%====================================================================================
request( start_engage, start_engage(ARG) ).
reply( engage_successful, engage_successful(ARG) ).  %%for start_engage
reply( engage_refused, engage_refused(ARG) ).  %%for start_engage
reply( retrylater, retrylater(CAUSE) ).  %%for start_engage
request( load_container, load_container(ARG) ).
reply( load_done, load_done(SLOT) ).  %%for load_container
request( ask_for_slot, ask_for_slot(ARG) ).
reply( slot_obtained, slot_obtained(SLOT) ).  %%for ask_for_slot
reply( slots_unavailable, slots_unavailable(ARG) ).  %%for ask_for_slot
dispatch( startWorking, startWorking(ARG) ).
dispatch( taskCompleted, taskCompleted(ARG) ).
request( start_marking, start_marking(ARG) ).
event( container_marked, container_marked(BARCODE) ).
event( robot_detected, robot_detected(DISTANCE) ).
event( sonar_failure, sonar_failure(DISTANCE) ).
event( system_failure, system_failure(ARG) ).
dispatch( show_hold_state, show_hold_state(STATE) ).
dispatch( show_service_status, show_service_status(STATUS) ).
dispatch( setrobotstate, setpos(X,Y,D) ).
request( moverobot, moverobot(SLOT) ).
request( moverobot, moverobot(TARGETX,TARGETY,STEPTIME) ).
reply( moverobotdone, moverobotdone(ARG) ).  %%for moverobot
reply( moverobotfailed, moverobotfailed(PLANDONE,PLANTODO) ).  %%for moverobot
%====================================================================================
context(ctx_cargoservice, "localhost",  "TCP", "8000").
context(ctx_smartrobot, "127.0.0.1",  "TCP", "8020").
context(ctx_ioport, "localhost",  "TCP", "8001").
context(ctx_client, "localhost",  "TCP", "8002").
context(ctx_devices, "localhost",  "TCP", "8003").
 qactor( robotsmart, ctx_smartrobot, "external").
  qactor( cargoservice_handler, ctx_cargoservice, "it.unibo.cargoservice_handler.Cargoservice_handler").
 static(cargoservice_handler).
  qactor( cargoservice_worker, ctx_cargoservice, "it.unibo.cargoservice_worker.Cargoservice_worker").
 static(cargoservice_worker).
  qactor( cargorobot, ctx_cargoservice, "it.unibo.cargorobot.Cargorobot").
 static(cargorobot).
  qactor( marker, ctx_cargoservice, "it.unibo.marker.Marker").
 static(marker).
  qactor( display, ctx_cargoservice, "it.unibo.display.Display").
 static(display).
  qactor( hold, ctx_cargoservice, "it.unibo.hold.Hold").
 static(hold).
  qactor( sonar, ctx_cargoservice, "it.unibo.sonar.Sonar").
 static(sonar).
  qactor( led, ctx_devices, "it.unibo.led.Led").
 static(led).
  qactor( pushbutton, ctx_ioport, "it.unibo.pushbutton.Pushbutton").
 static(pushbutton).
  qactor( io_port, ctx_cargoservice, "it.unibo.io_port.Io_port").
 static(io_port).
  qactor( external_client, ctx_client, "it.unibo.external_client.External_client").
 static(external_client).
