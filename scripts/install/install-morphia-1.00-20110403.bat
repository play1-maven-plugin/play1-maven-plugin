set MODULE_NAME=morphia
set MODULE_VERSION=1.2.4b
set SRC_DIR=../poms/modules/%MODULE_NAME%

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%com.google.code.morphia
set ARTIFACT_ID=morphia
set VERSION=1.00-20110403

call mvn install:install-file -Dfile=%SRC_DIR%/%ARTIFACT_ID%-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=%SRC_DIR%/%ARTIFACT_ID%-%VERSION%.pom
call mvn install:install-file -Dfile=%SRC_DIR%/%ARTIFACT_ID%-%VERSION%-sources.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -Dclassifier=sources -DgeneratePom=false
