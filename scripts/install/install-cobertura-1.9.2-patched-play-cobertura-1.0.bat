call ..\set-external-modules-home.bat
set MODULE_NAME=cobertura
set MODULE_VERSION=2.4
set SRC_DIR=%MODULES_HOME%/%MODULE_NAME%-%MODULE_VERSION%/lib

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%net.sourceforge.cobertura
set ARTIFACT_ID=cobertura
set VERSION=1.9.2-patched-play-cobertura-1.0

call mvn install:install-file -Dfile=%SRC_DIR%/cobertura-main.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%.pom
