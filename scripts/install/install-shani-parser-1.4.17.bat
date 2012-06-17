rem call ..\set-external-modules-home.bat
set MODULE_NAME=pdf
rem set MODULE_VERSION=0.9
set SRC_DIR=../sources/shani-parser/v1.4.17/ShaniXmlParser/ShaniXmlParser

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%org.allcolor.shanidom
set ARTIFACT_ID=shani-parser
set VERSION=1.4.17

call mvn clean package source:jar javadoc:jar --file %SRC_DIR%/pom.xml
call mvn install:install-file -Dfile=%SRC_DIR%/../CommonLibraries/lib/xml/%ARTIFACT_ID%-v%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%.pom -Dsources=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-sources.jar -Djavadoc=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-javadoc.jar
