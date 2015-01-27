call ..\set-play-home-1.3.0.bat

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%org.hibernate
set ARTIFACT_ID=hibernate-core
set BASE_VERSION=4.2.15.Final
set VERSION=%BASE_VERSION%-patched-play-1.3.0

set SRC_DIR=../sources/%ARTIFACT_ID%/%VERSION%/patched-play-1.3.0/hibernate-core

call mvn install:install-file -Dfile=%PLAY_HOME%/framework/lib/%ARTIFACT_ID%-%BASE_VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/play/%ARTIFACT_ID%-%VERSION%.pom -Dsources=%SRC_DIR%/target/libs/%ARTIFACT_ID%-%BASE_VERSION%-sources.jar -Djavadoc=%SRC_DIR%/target/libs/%ARTIFACT_ID%-%BASE_VERSION%-javadoc.jar
