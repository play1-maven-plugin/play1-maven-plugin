call ..\set-external-modules-home.bat
set MODULE_NAME=siena
set MODULE_VERSION=2.0.7
set SRC_DIR=%MODULES_HOME%/%MODULE_NAME%-%MODULE_VERSION%/lib

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%com.sienaproject.siena
set ARTIFACT_ID=siena
set VERSION=1.0.0-b6-20111107

call mvn install:install-file -Dfile=%SRC_DIR%/%ARTIFACT_ID%-1.0.0-b6.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%.pom
