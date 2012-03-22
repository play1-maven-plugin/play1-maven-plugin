call ..\set-external-modules-home.bat
set MODULE_NAME=morphia
set VERSION=1.2.5a
set JAR_FILE_NAME=play-%MODULE_NAME%-%VERSION%

call deploy-external-module-with-jar.bat
