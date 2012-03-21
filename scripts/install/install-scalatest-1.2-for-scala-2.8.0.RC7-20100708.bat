call ..\set-external-modules-home.bat
set MODULE_NAME=scala
set MODULE_VERSION=0.9.1
set SRC_DIR=%MODULES_HOME%/%MODULE_NAME%-%MODULE_VERSION%/lib

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%org.scalatest
set ARTIFACT_ID=scalatest
set VERSION=1.2-for-scala-2.8.0.RC7-20100708

call mvn install:install-file -Dfile=%SRC_DIR%/%ARTIFACT_ID%-1.2-for-scala-2.8.0.RC7-SNAPSHOT.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%.pom -Dsources=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%-sources.jar -Djavadoc=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%-javadoc.jar
call mvn install:install-file -Dfile=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%-scaladoc.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -Dclassifier=scaladoc -DgeneratePom=false
