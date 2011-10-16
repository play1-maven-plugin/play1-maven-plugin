call ..\set-play-home-1.1.bat
set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%org.eclipse.jdt
set ARTIFACT_ID=org.eclipse.jdt.core
set VERSION=3.6.0.v_A56

set REPO_ID=com.google.code.maven-play-plugin
set REPO_URL=https://maven-play-plugin.googlecode.com/svn/mavenrepo/releases
@rem set REPO_ID=com.google.code.maven-play-plugin-snapshots
@rem set REPO_URL=https://maven-play-plugin.googlecode.com/svn/mavenrepo/snapshots

call mvn deploy:deploy-file -Dfile=%PLAY_HOME%/framework/lib/org.eclipse.jdt.core_3.6.0.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/play/%ARTIFACT_ID%-%VERSION%.pom -DrepositoryId=%REPO_ID% -Durl=dav:%REPO_URL% -e
