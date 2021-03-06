live-demo-java
==============


Requirements
------------

* Maven 3
* Java 7


How to compile
--------------

Download the sources in a directory ($SOURCES)

    cd $SOURCES
    mvn install


Created artifacts
-----------------

* **demo-1.0-SNAPSHOT.jar**: demo jar
* **demo-bundle.jar**: demo with all dependencies bundled


Running the demo
----------------

  $ java -jar demo-bundle.jar

The demo-bundle will, initially, create the following entities:
* 4 technicians
* 4 vans
* 4 vending machines

After 5 seconds it will start creating 4 issues, one every 2 seconds;
then it will subscribe to receive notification about new issues using the URL given at command line.
Once received a notification the demo will extract the vanID associated to the technitian selected for the issue
and will start moving the van according to the build-in routing file.

Notes
=====

You can find the Java library used for accessing the NGSI server at [wirecloud-fiware/ngsijava](https://github.com/wirecloud-fiware/ngsijava)
