call ..\set-external-modules-home.bat
set MODULE_NAME=morphia
set MODULE_VERSION=1.2.4b
set SRC_DIR=%MODULES_HOME%/%MODULE_NAME%-%MODULE_VERSION%/lib

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%com.github.greenlaw110.org.mongodb
set ARTIFACT_ID=mongo-java-driver
set VERSION=2.6.5a

call mvn install:install-file -Dfile=%SRC_DIR%/mongo-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%.pom
