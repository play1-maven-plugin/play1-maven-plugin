call ..\set-play-home-1.2.3.bat
set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%postgresql
set ARTIFACT_ID=postgresql
set VERSION=9.0-801.jdbc4-patched-play-1.2.3

call mvn install:install-file -Dfile=%PLAY_HOME%/framework/lib/postgresql-9.0.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../%ARTIFACT_ID%-%VERSION%.pom
