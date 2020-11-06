@echo off
title SVG Flow Patch

set "ROOT_PATH=%~dp0"
cd /d "%ROOT_PATH%"

"../../jdk/bin/java" -classpath seeyon-svg-patch.jar;%ROOT_PATH%/lib/log4j.jar;%ROOT_PATH%/lib/fastjson.jar;%ROOT_PATH%/lib/ant.jar SvgPatch

pause