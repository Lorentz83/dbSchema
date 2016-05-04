dbSchema
========

dbSchema is a library written in java which can parse sql queries to
extract a set of features that may be used to implement anomaly
detection engines.

This library is released under the MIT license.

Features
--------

Once the schema is imported, this library can resolve the columns to
the tables they belong.
For example, given the query:
```
SELECT a, b
FROM t1 JOIN t2
```
the schema is required to correctly map the columns `a` and `b` to the
table they belongs.

If users, roles and grants are provided, this library can return the
list of roles used to execute the query.
Roles can be organized in an hierarchical way.

Dependencies
------------

This project is written in java and must be compiled using JDK 8 and
maven.

This project depends on the library
[General Sql Parser v1.6.3.1 for Java](http://www.sqlparser.com/).
The trial version can be downloaded and installed into the local maven
repository executing the shell script: ``install_dependencies.sh``

All the other dependencies are automatically downloaded by maven.

Compile
-------

Once the all the dependencies are correctly installed, just type on a
shell `mvn package`. The compiled jar file will be generated into
`target`.

To create the documentation execute `mvn javadoc:javadoc`.

You can check the test coverage opening the file
`target/site/jacoco/index.html`.

Acknowledgment
--------------

This library has been written as part of a bigger project developed by
the research team of Professor Elisa Bertino at the [Department of
Computer Science of Purdue University](https://www.cs.purdue.edu/).
