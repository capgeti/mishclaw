#!/bin/bash

mvn exec:java -Dexec.args="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
