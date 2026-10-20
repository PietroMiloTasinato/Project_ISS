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
with Diagram('sprint_1Arch', show=False, outformat='png', graph_attr=graphattr) as diag:
  with Cluster('env'):
     sys = Custom('','./qakicons/system.png')
### see https://renenyffenegger.ch/notes/tools/Graphviz/attributes/label/HTML-like/index
     with Cluster('ctx_cargoservice', graph_attr=nodeattr):
          cargoservice=Custom('cargoservice','./qakicons/symActorWithobjSmall.png')
          marker=Custom('marker','./qakicons/symActorWithobjSmall.png')
          hold=Custom('hold','./qakicons/symActorWithobjSmall.png')
          io_port=Custom('io_port','./qakicons/symActorWithobjSmall.png')
          sonar_mock=Custom('sonar_mock','./qakicons/symActorWithobjSmall.png')
          led_mock=Custom('led_mock','./qakicons/symActorWithobjSmall.png')
     with Cluster('ctx_smartrobot', graph_attr=nodeattr):
          robotsmart=Custom('robotsmart(ext)','./qakicons/externalQActor.png')
     cargoservice >> Edge( label='ioport_occupied', **eventedgeattr, decorate='true', fontcolor='red') >> sys
     cargoservice >> Edge(color='magenta', style='solid', decorate='true', label='<start_marking<font color="darkgreen"> container_marked</font> &nbsp; >',  fontcolor='magenta') >> marker
     cargoservice >> Edge(color='magenta', style='solid', decorate='true', label='<moverobot<font color="darkgreen"> moverobotdone moverobotfailed</font> &nbsp; >',  fontcolor='magenta') >> robotsmart
     cargoservice >> Edge(color='magenta', style='solid', decorate='true', label='<detect_container<font color="darkgreen"> container_detected sonar_timeout</font> &nbsp; >',  fontcolor='magenta') >> sonar_mock
     io_port >> Edge(color='magenta', style='solid', decorate='true', label='<request_to_load<font color="darkgreen"> retrylater rejected engaged</font> &nbsp; >',  fontcolor='magenta') >> cargoservice
     cargoservice >> Edge(color='magenta', style='solid', decorate='true', label='<ask_for_slot<font color="darkgreen"> slot_assigned hold_full</font> &nbsp; ask_for_point<font color="darkgreen"> point_found point_not_found</font> &nbsp; >',  fontcolor='magenta') >> hold
     cargoservice >> Edge(color='blue', style='solid',  decorate='true', label='<setrobotstate &nbsp; >',  fontcolor='blue') >> robotsmart
     cargoservice >> Edge(color='blue', style='solid',  decorate='true', label='<stop_blinking &nbsp; start_blinking &nbsp; >',  fontcolor='blue') >> led_mock
     cargoservice >> Edge(color='blue', style='solid',  decorate='true', label='<container_deployed &nbsp; >',  fontcolor='blue') >> hold
diag
