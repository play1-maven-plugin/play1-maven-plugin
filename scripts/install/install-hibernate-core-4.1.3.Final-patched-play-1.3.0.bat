call ..\set-play-home-1.3.0-RC1.bat
set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%org.hibernate
set ARTIFACT_ID=hibernate-core
set VERSION=4.1.3.Final-patched-play-1.3.0

set SRC_DIR=../sources/hibernate-core/4.1.3.Final/patched-play-1.3.0/hibernate-core

call mvn install:install-file -Dfile=%PLAY_HOME%/framework/lib/hibernate-core-4.1.3.Final.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/play/%ARTIFACT_ID%-%VERSION%.pom -Dsources=%SRC_DIR%/target/libs/%ARTIFACT_ID%-4.1.3.Final-sources.jar -Djavadoc=%SRC_DIR%/target/libs/%ARTIFACT_ID%-4.1.3.Final-javadoc.jar
