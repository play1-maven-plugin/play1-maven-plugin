call ..\set-external-modules-home.bat
set MODULE_NAME=rythm
@rem set MODULE_VERSION=undefined
set SRC_DIR=../sources/pat/pat-1.5.3

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%com.javaregex.pat
set ARTIFACT_ID=pat
set VERSION=1.5.3

call mvn clean package source:jar javadoc:jar --file %SRC_DIR%/pom.xml
call mvn install:install-file -Dfile=%SRC_DIR%/../patbinfree153.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%.pom -Dsources=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-sources.jar -Djavadoc=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-javadoc.jar
