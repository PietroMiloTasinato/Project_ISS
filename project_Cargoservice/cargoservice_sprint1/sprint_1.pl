%====================================================================================
% sprint_1 description   
%====================================================================================
request( request_to_load, request_to_load(ARG) ).
reply( retrylater, retrylater(ARG) ).  %%for request_to_load
reply( rejected, rejected(ARG) ).  %%for request_to_load
reply( engaged, engaged(SLOT) ).  %%for request_to_load
request( ask_for_slot, ask_for_slot(ARG) ).
reply( slot_assigned, slot_assigned(ID) ).  %%for ask_for_slot
reply( hold_full, hold_full(ARG) ).  %%for ask_for_slot
request( ask_for_point, ask_for_point(ID) ).
reply( point_found, point_found(X,Y) ).  %%for ask_for_point
reply( point_not_found, point_not_found(ARG) ).  %%for ask_for_point
dispatch( container_deployed, container_deployed(SlotId,Code) ).
dispatch( start_blinking, start_blinking(ARG) ).
dispatch( stop_blinking, stop_blinking(ARG) ).
request( detect_container, detect_container(ARG) ).
reply( container_detected, container_detected(ARG) ).  %%for detect_container
reply( sonar_timeout, sonar_timeout(ARG) ).  %%for detect_container
dispatch( setrobotstate, setpos(X,Y,D) ).
request( moverobot, moverobot(TARGETX,TARGETY,STEPTIME) ).
reply( moverobotdone, moverobotdone(ARG) ).  %%for moverobot
reply( moverobotfailed, moverobotfailed(PLANDONE,PLANTODO) ).  %%for moverobot
event( ioport_occupied, io_port_occupied(ARG) ).
request( start_marking, start_marking(ARG) ).
reply( container_marked, container_marked(BARCODE) ).  %%for start_marking
%====================================================================================
context(ctx_cargoservice, "localhost",  "TCP", "8000").
context(ctx_smartrobot, "127.0.0.1",  "TCP", "8020").
 qactor( robotsmart, ctx_smartrobot, "external").
  qactor( cargoservice, ctx_cargoservice, "it.unibo.cargoservice.Cargoservice").
 static(cargoservice).
  qactor( marker, ctx_cargoservice, "it.unibo.marker.Marker").
 static(marker).
  qactor( hold, ctx_cargoservice, "it.unibo.hold.Hold").
 static(hold).
  qactor( io_port, ctx_cargoservice, "it.unibo.io_port.Io_port").
 static(io_port).
  qactor( sonar_mock, ctx_cargoservice, "it.unibo.sonar_mock.Sonar_mock").
 static(sonar_mock).
  qactor( led_mock, ctx_cargoservice, "it.unibo.led_mock.Led_mock").
 static(led_mock).
