call ..\set-external-modules-home.bat
set MODULE_NAME=pdf
set SRC_DIR=../sources/yahp/1.3/YaHPConverter

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%org.allcolor.yahp
set ARTIFACT_ID=yahp-internal
set VERSION=1.3

call mvn clean package source:jar javadoc:jar --file %SRC_DIR%/pom-internal.xml
call mvn install:install-file -Dfile=../sources/yahp/binaries-1.3/%ARTIFACT_ID%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%.pom -Dsources=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-sources.jar -Djavadoc=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-javadoc.jar
