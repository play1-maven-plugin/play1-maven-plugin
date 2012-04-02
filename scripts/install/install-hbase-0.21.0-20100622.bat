call ..\set-external-modules-home.bat
set MODULE_NAME=siena
rem set MODULE_VERSION=2.0.7
set SRC_DIR=../poms/modules/%MODULE_NAME%

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%org.apache.hbase
set ARTIFACT_ID=hbase
set VERSION=0.21.0-20100622

call mvn install:install-file -Dfile=%SRC_DIR%/%ARTIFACT_ID%-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=%SRC_DIR%/%ARTIFACT_ID%-%VERSION%.pom -Dsources=%SRC_DIR%/%ARTIFACT_ID%-%VERSION%-sources.jar
call mvn install:install-file -Dfile=%SRC_DIR%/%ARTIFACT_ID%-%VERSION%-tests.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -Dclassifier=tests -DgeneratePom=false
