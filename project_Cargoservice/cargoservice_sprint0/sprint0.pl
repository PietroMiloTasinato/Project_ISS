%====================================================================================
% sprint0 description   
%====================================================================================
request( request_to_load, request_to_load(ARG) ).
reply( retry_later, retry_later(ARG) ).  %%for request_to_load
reply( rejected, rejected(ARG) ).  %%for request_to_load
reply( engaged, engaged(SLOT) ).  %%for request_to_load
%====================================================================================
context(ctx_cargoservice, "localhost",  "TCP", "8000").
 qactor( cargoservice, ctx_cargoservice, "it.unibo.cargoservice.Cargoservice").
 static(cargoservice).
  qactor( io_port_mock, ctx_cargoservice, "it.unibo.io_port_mock.Io_port_mock").
 static(io_port_mock).
