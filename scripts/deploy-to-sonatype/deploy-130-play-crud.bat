set VERSION=1.3.0
call ..\set-play-home-%VERSION%.bat

set MODULE_NAME=crud

call deploy-play-module-without-jar.bat
