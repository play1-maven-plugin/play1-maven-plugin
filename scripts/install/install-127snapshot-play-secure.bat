set VERSION=1.2.7-SNAPSHOT
call ..\set-play-home-%VERSION%.bat

set MODULE_NAME=secure

call install-play-module-without-jar.bat
