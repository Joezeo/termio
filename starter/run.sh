#!/bin/bash
%JAVA17_HOME%/bin/java -jar -Xdebug -Xrunjdwp:transport=dt_socket,suspend=n,server=y,address=5500 ../target/terminatio.jar bash
