call ..\set-external-modules-home.bat
set MODULE_NAME=scala
set MODULE_VERSION=0.9.1
set SRC_DIR=%MODULES_HOME%/%MODULE_NAME%-%MODULE_VERSION%/lib

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%se.scalablesolutions.akka
set ARTIFACT_ID=akka-actor
set VERSION=1.0

call mvn install:install-file -Dfile=%SRC_DIR%/%ARTIFACT_ID%-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%.pom -Dsources=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%-sources.jar
call mvn install:install-file -Dfile=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%-docs.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -Dclassifier=scaladoc -DgeneratePom=false
