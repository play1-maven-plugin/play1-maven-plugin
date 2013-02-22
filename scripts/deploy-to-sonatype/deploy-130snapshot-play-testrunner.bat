set VERSION=1.3.0-SNAPSHOT
call ..\set-play-home-%VERSION%.bat

set MODULE_NAME=testrunner

call deploy-play-module-with-jar-snapshot.bat
