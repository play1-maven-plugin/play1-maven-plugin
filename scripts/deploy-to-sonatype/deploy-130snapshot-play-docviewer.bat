set VERSION=1.3.0-SNAPSHOT
call ..\set-play-home-%VERSION%.bat

set MODULE_NAME=docviewer

call deploy-play-module-without-jar-snapshot.bat