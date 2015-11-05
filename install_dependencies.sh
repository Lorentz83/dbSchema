#!/bin/sh
set -e

echo "Downloading General Sql Parser java library"
cd /tmp
wget 'http://www.sqlparser.com/dl/gsp_java_trial_1_6_3_1.zip'

echo "Decompressing the library"
unzip gsp_java_trial_1_6_3_1.zip
cd dist

echo "Creating javadoc"
(cd javadoc && zip -r ../javadoc.zip *)

echo "Installing the library to the local maven repository"
mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
    -Dfile=gsp.jar \
    -DgroupId=com.dpriver \
    -DartifactId=gsp \
    -Dversion=1.6.3.1 \
    -Dpackaging=jar \
    -Djavadoc=javadoc.zip
