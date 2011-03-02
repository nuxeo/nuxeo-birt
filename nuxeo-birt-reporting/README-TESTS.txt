Maven is used to pull the required dependecies as a ZIP from Nexus, it works for running the tests via Surefire and to build the jar,
but it does not work for running the JUnit tests directly from Eclipse.

If you want to run the tests directly from eclipse, here is what you need to do :

1 - first run the build/test with maven

  mvn -o clean install

2 - copy the birt runtime zip in the resources dir

  cp target/classes/birt-runtime-all-2.6.1.zip src/main/resources/.

NB : The zip is added as dependency by maven, but is not added in eclipse classpath because of http://jira.codehaus.org/browse/MECLIPSE-406
