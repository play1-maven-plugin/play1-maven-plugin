call ..\set-play-home-1.1.bat
set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%org.eclipse.jdt
set ARTIFACT_ID=org.eclipse.jdt.core
set VERSION=3.6.0.v_A56

call mvn install:install-file -Dfile=%PLAY_HOME%/framework/lib/org.eclipse.jdt.core_3.6.0.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/play/%ARTIFACT_ID%-%VERSION%.pom
