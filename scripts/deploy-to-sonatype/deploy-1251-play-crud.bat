set VERSION=1.2.5.1
call ..\set-play-home-%VERSION%.bat

set MODULE_NAME=crud

call deploy-play-module-without-jar.bat
