set VERSION=1.2.6-SNAPSHOT
call ..\set-play-home-%VERSION%.bat

set MODULE_NAME=grizzly

call deploy-play-module-with-jar-snapshot.bat
