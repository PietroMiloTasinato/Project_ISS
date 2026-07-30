### conda install diagrams
from diagrams import Cluster, Diagram, Edge
from diagrams.custom import Custom
import os

os.environ['PATH'] += os.pathsep + '/usr/lib/x86_64-linux-gnu/'
#os.environ['PATH'] += os.pathsep + 'C:/Program Files/Graphviz/bin/'


graphattr = {     #https://www.graphviz.org/doc/info/attrs.html
    'fontsize': '22',
}

nodeattr = {   
    'fontsize': '22',
    'bgcolor': 'lightyellow'
}

eventedgeattr = {
    'color': 'red',
}

evattr = {
    'color': 'darkgreen',
    'style': 'dotted'
}

with Diagram('Sprint0_Arch', show=False, outformat='png', graph_attr=graphattr) as diag:
    with Cluster('env'):
        sys = Custom('','./qakicons/system.png')

        with Cluster('ctx_cargoservice', graph_attr=nodeattr):
            cargoservice=Custom('cargoservice','./qakicons/symActorSmall.png')
            cargorobot=Custom('cargorobot','./qakicons/symActorSmall.png')
            hold=Custom('hold','./qakicons/symActorSmall.png')
            marker=Custom('marker','./qakicons/symActorSmall.png')
            sonar=Custom('sonar','./qakicons/symActorSmall.png')
        with Cluster('ctx_ioPort', graph_attr=nodeattr):
            ioPort=Custom('ioPort','./qakicons/symActorSmall.png')
            display=Custom('display','./qakicons/symActorSmall.png')
            pushbutton=Custom('pushbutton','./qakicons/symActorSmall.png')
        with Cluster('ctx_devices', graph_attr=nodeattr):
            led=Custom('led','./qakicons/symActorSmall.png')
        with Cluster('ctx_client', graph_attr=nodeattr):
            external_client=Custom('external_client','./qakicons/symActorSmall.png')

        pushbutton >> Edge(color='magenta', style='solid', decorate='false', label='<load_product<font color="darkgreen"> load_accepted load_refused</font> &nbsp; >',  fontcolor='magenta') >> cargoservice
        cargoservice >> Edge(color='magenta', style='solid', decorate='false', label='<show_service_status(STATUS) &nbsp; >',  fontcolor='magenta') >> display
        hold >> Edge(color='magenta', style='solid', decorate='false', label='<show_hold_status(STATUS) &nbsp; >',  fontcolor='magenta') >> display
        sonar >> Edge(**eventedgeattr, style='solid', decorate='false', label='<container_detected(DISTANCE) &nbsp; >',  fontcolor='red') >> sys
        marker >> Edge(**eventedgeattr, style='solid', decorate='false', label='<container_marked(BARCODE) &nbsp; >', fontcolor='red') >> sys
        sonar >> Edge( label='<sonar_failure(DISTANCE)&nbsp; >', **eventedgeattr, style='solid', decorate='false', fontcolor='red') >> sys
diag
