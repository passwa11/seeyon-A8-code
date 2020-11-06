#!/bin/bash

. /etc/profile

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
classpath=seeyon-svg-patch.jar:$DIR/lib/log4j.jar:$DIR/lib/fastjson.jar:$DIR/lib/ant.jar:
./../../jdk/bin/java -classpath $classpath SvgPatch