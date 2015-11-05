dbSchema
========

dbSchema is a library written in java which can parse sql queries to
extract a set of features that may be used to implement anomaly
detection engines.

### Dependencies
This project is written in java and must be compiled using JDK 8 and
maven.

This project depends by the library
[General Sql Parser v1.6.3.1 for Java](http://www.sqlparser.com/).
The trial version can be downloaded and installed into the local maven
repository executing the shell script: ``install_dependencies.sh``

All the other dependencies are automatically downloaded by maven.

### Compile

Once the all the dependencies are correctly installed, just type on a
shell ``mvn package``. The compiled jar file will be generated into
``target``.

To create the documentation execute ``mvn javadoc:javadoc``.
