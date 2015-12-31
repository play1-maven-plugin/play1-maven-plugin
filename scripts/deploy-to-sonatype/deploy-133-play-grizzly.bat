set VERSION=1.3.3
call ..\set-play-home-%VERSION%.bat

set MODULE_NAME=grizzly

call deploy-play-module-with-jar.bat
