call ..\set-play-home-1.2.5.bat
set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%org.apache.commons
set ARTIFACT_ID=commons-javaflow
set VERSION=1209105
set SRC_DIR=../sources/commons-javaflow/trunk-rel.1209105

call mvn clean source:jar javadoc:jar --file %SRC_DIR%/pom.xml
rem call mvn clean package source:jar javadoc:jar --file %SRC_DIR%/pom.xml
call mvn install:install-file -Dfile=%PLAY_HOME%/framework/lib/%ARTIFACT_ID%-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=%SRC_DIR%/pom.xml -Dsources=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-sources.jar -Djavadoc=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-javadoc.jar
