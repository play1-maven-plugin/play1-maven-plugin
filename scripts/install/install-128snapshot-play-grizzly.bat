set VERSION=1.2.8-SNAPSHOT
call ..\set-play-home-%VERSION%.bat

set MODULE_NAME=grizzly

call install-play-module-with-jar.bat
