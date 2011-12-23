call ..\set-external-modules-home.bat
set MODULE_NAME=webdrive
set VERSION=0.2
set JAR_FILE_NAME=%MODULE_NAME%-%VERSION%

call deploy-external-module-with-jar-without-min.bat
