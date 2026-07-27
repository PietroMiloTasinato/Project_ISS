### conda install diagrams
from diagrams import Cluster, Diagram, Edge
from diagrams.custom import Custom
import os
os.environ['PATH'] += os.pathsep + 'C:/Program Files/Graphviz/bin/'

graphattr = {     #https://www.graphviz.org/doc/info/attrs.html
    'fontsize': '22',
}

nodeattr = {   
    'fontsize': '22',
    'bgcolor': 'lightyellow'
}

eventedgeattr = {
    'color': 'red',
    'style': 'dotted'
}
evattr = {
    'color': 'darkgreen',
    'style': 'dotted'
}
with Diagram('sprint0Arch', show=False, outformat='png', graph_attr=graphattr) as diag:
  with Cluster('env'):
     sys = Custom('','./qakicons/system.png')
### see https://renenyffenegger.ch/notes/tools/Graphviz/attributes/label/HTML-like/index
     with Cluster('ctx_cargoservice', graph_attr=nodeattr):
          cargoservice=Custom('cargoservice','./qakicons/symActorWithobjSmall.png')
          cargorobot=Custom('cargorobot','./qakicons/symActorWithobjSmall.png')
          marker=Custom('marker','./qakicons/symActorWithobjSmall.png')
          hold=Custom('hold','./qakicons/symActorWithobjSmall.png')
          sonar=Custom('sonar','./qakicons/symActorWithobjSmall.png')
     with Cluster('ctx_ioport', graph_attr=nodeattr):
          display=Custom('display','./qakicons/symActorWithobjSmall.png')
          pushbutton=Custom('pushbutton','./qakicons/symActorWithobjSmall.png')
          ioport=Custom('ioport','./qakicons/symActorWithobjSmall.png')
     with Cluster('ctx_client', graph_attr=nodeattr):
          external_client=Custom('external_client','./qakicons/symActorWithobjSmall.png')
     with Cluster('ctx_devices', graph_attr=nodeattr):
          led=Custom('led','./qakicons/symActorWithobjSmall.png')
     marker >> Edge( label='container_marked', **eventedgeattr, decorate='true', fontcolor='red') >> sys
     cargoservice >> Edge(color='blue', style='solid',  decorate='true', label='<show_service_status &nbsp; >',  fontcolor='blue') >> display
diag
