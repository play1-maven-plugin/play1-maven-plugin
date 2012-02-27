call ..\set-external-modules-home.bat
set MODULE_NAME=less
set MODULE_VERSION=0.9
set SRC_DIR=%MODULES_HOME%/%MODULE_NAME%-%MODULE_VERSION%/lib

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%com.asual.lesscss
set ARTIFACT_ID=lesscss-engine
set VERSION=1.2.1-20120201

call mvn install:install-file -Dfile=%SRC_DIR%/%ARTIFACT_ID%-1.2.1-SNAPSHOT.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%.pom
