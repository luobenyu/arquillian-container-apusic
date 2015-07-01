
#Arquillian Containers Adapter for Apusic
 
##Initial feature##
<br/>
Only support remoted Apusic Application Server.

Configuration for Apusic container arquillian.xml:<br/>
- property "user" and "password" are mandatory<br/>
- property "host" and "port" are optional, default value is "localhost" and 6888.


##ChangeLog##


July,1, 2015<br/>
added @ApusicDeployment annotation for @Deployment, to support Apusic 'globalSession' feature, as well as
    'virtualhost', 'starttype' etc. deploying parameters.<br/>
support multi war archive deploying with @ApusicDeployment