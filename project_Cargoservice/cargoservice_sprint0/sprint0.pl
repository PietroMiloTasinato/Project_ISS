%====================================================================================
% sprint0 description   
%====================================================================================
request( load_container, load_container(ARG) ).
reply( load_accepted, load_accepted(SLOT) ).  %%for load_container
reply( load_refused, load_refused(CAUSE) ).  %%for load_container
reply( retrylater, retrylater(CAUSE) ).  %%for load_container
dispatch( move_container_to_slot, move_container_to_slot(SLOT) ).
dispatch( startWorking, startWorking(ARG) ).
dispatch( taskCompleted, taskCompleted(ARG) ).
event( container_marked, container_marked(BARCODE) ).
event( container_detected, container_detected(DISTANCE) ).
event( sonar_failure, sonar_failure(DISTANCE) ).
dispatch( show_hold_state, show_hold_state(STATE) ).
dispatch( show_service_status, show_service_status(STATUS) ).
%====================================================================================
context(ctx_cargoservice, "localhost",  "TCP", "8000").
context(ctx_ioport, "localhost",  "TCP", "8001").
context(ctx_client, "localhost",  "TCP", "8002").
context(ctx_devices, "localhost",  "TCP", "8003").
 qactor( cargoservice_handler, ctx_cargoservice, "it.unibo.cargoservice_handler.Cargoservice_handler").
 static(cargoservice_handler).
  qactor( cargoservice_worker, ctx_cargoservice, "it.unibo.cargoservice_worker.Cargoservice_worker").
 static(cargoservice_worker).
  qactor( cargorobot, ctx_cargoservice, "it.unibo.cargorobot.Cargorobot").
 static(cargorobot).
  qactor( marker, ctx_cargoservice, "it.unibo.marker.Marker").
 static(marker).
  qactor( display, ctx_ioport, "it.unibo.display.Display").
 static(display).
  qactor( hold, ctx_cargoservice, "it.unibo.hold.Hold").
 static(hold).
  qactor( sonar, ctx_cargoservice, "it.unibo.sonar.Sonar").
 static(sonar).
  qactor( led, ctx_devices, "it.unibo.led.Led").
 static(led).
  qactor( pushbutton, ctx_ioport, "it.unibo.pushbutton.Pushbutton").
 static(pushbutton).
  qactor( ioport, ctx_ioport, "it.unibo.ioport.Ioport").
 static(ioport).
  qactor( external_client, ctx_client, "it.unibo.external_client.External_client").
 static(external_client).
