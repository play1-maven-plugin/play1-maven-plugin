set VERSION=1.2.6.2
call ..\set-play-home-%VERSION%.bat

set MODULE_NAME=grizzly

call deploy-play-module-with-jar.bat
