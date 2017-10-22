set VERSION=1.4.5
call ..\set-play-home-%VERSION%.bat

set MODULE_NAME=grizzly

call deploy-play-module-with-jar.bat
