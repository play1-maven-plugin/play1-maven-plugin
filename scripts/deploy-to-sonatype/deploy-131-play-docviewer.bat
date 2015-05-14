set VERSION=1.3.1
call ..\set-play-home-%VERSION%.bat

set MODULE_NAME=docviewer

call deploy-play-module-with-jar.bat
