set VERSION=1.4.5
call ..\set-play-home-%VERSION%.bat

set MODULE_NAME=secure

call deploy-play-module-without-jar.bat
