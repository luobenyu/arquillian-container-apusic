# arquillian-container-apusic
Arquillian Containers Adapter for Apusic


Only support remoted Apusic Application Server right now.


Configuration for Apusic container arquillian.xml:
property "user" and "password" are mandatory
property "host" and "port" are optional, default value is "localhost" and 6888.

July,1, 2015
added @ApusicDeployment annotation for @Deployment, to support Apusic 'globalSession' feature, as well as
    'virtualhost', 'starttype' etc. deploying parameters.
support multi war archive deploying with @ApusicDeployment