set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%spy
set ARTIFACT_ID=spymemcached
set VERSION=2.6
set SRC_DIR=../sources/spymemcached

call mvn install:install-file -Dfile=%SRC_DIR%/%ARTIFACT_ID%-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/play/%ARTIFACT_ID%-%VERSION%.pom -Dsources=%SRC_DIR%/%ARTIFACT_ID%-%VERSION%-sources.jar -Djavadoc=%SRC_DIR%/%ARTIFACT_ID%-%VERSION%-javadoc.jar
