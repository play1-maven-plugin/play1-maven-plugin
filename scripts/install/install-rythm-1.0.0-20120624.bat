call ..\set-external-modules-home.bat
set MODULE_NAME=rythm
set MODULE_VERSION=1.0.0-RC7
set SRC_DIR=../sources/rythm/1.0.0-20120624

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%com.rythmengine
set ARTIFACT_ID=rythm
set VERSION=1.0.0-20120624

call mvn clean package source:jar javadoc:jar --file %SRC_DIR%/pom.xml
call mvn install:install-file -Dfile=%MODULES_HOME%/%MODULE_NAME%-%MODULE_VERSION%/lib/%ARTIFACT_ID%-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%.pom -Dsources=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-sources.jar -Djavadoc=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-javadoc.jar
