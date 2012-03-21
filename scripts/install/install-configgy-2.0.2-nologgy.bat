call ..\set-external-modules-home.bat
set MODULE_NAME=scala
set MODULE_VERSION=0.9.1
set SRC_DIR=%MODULES_HOME%/%MODULE_NAME%-%MODULE_VERSION%/lib

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%net.lag
set ARTIFACT_ID=configgy
set VERSION=2.0.2-nologgy

call mvn install:install-file -Dfile=%SRC_DIR%/%ARTIFACT_ID%-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%.pom
