call ..\set-play-home-1.2.3.bat
set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%postgresql
set ARTIFACT_ID=postgresql
set VERSION=9.0-801.jdbc4-patched-play-1.2.3
set SRC_DIR=../sources/postgresql/9.0-801.jdbc4

call mvn install:install-file -Dfile=%PLAY_HOME%/framework/lib/postgresql-9.0.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/play/%ARTIFACT_ID%-%VERSION%.pom -Dsources=%SRC_DIR%/%ARTIFACT_ID%-%VERSION%-sources.jar -Djavadoc=%SRC_DIR%/%ARTIFACT_ID%-%VERSION%-javadoc.jar
@rem call mvn install:install-file -Dfile=%SRC_DIR%/%ARTIFACT_ID%-%VERSION%-javadoc.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -Dclassifier=javadoc -DgeneratePom=false
@rem call mvn install:install-file -Dfile=%SRC_DIR%/%ARTIFACT_ID%-%VERSION%-sources.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -Dclassifier=sources -DgeneratePom=false

