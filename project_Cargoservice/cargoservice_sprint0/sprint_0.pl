%====================================================================================
% sprint_0 description   
%====================================================================================
request( request_to_load, request_to_load(ARG) ).
reply( retry_later, retry_later(ARG) ).  %%for request_to_load
reply( rejected, rejected(ARG) ).  %%for request_to_load
reply( engaged, engaged(SLOT) ).  %%for request_to_load
request( ask_for_slot, ask_for_slot(ARG) ).
reply( slot_assigned, slot_assigned(SLOT) ).  %%for ask_for_slot
reply( hold_full, hold_full(ARG) ).  %%for ask_for_slot
dispatch( set_slot, set_slot(VALUE) ).
%====================================================================================
context(ctx_cargoservice, "localhost",  "TCP", "8000").
 qactor( cargo_service, ctx_cargoservice, "it.unibo.cargo_service.Cargo_service").
 static(cargo_service).
  qactor( hold, ctx_cargoservice, "it.unibo.hold.Hold").
 static(hold).
