call ..\set-play-home-1.2.5.bat
set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%org.hibernate
set ARTIFACT_ID=hibernate-core
set VERSION=3.6.10.Final-patched-play-1.2.5
set SRC_DIR=../sources/hibernate-core/3.6.10.Final/core.patched-play-1.2.5

set REPO_ID=com.google.code.maven-play-plugin
set REPO_URL=https://maven-play-plugin.googlecode.com/svn/mavenrepo/releases
@rem set REPO_ID=com.google.code.maven-play-plugin-snapshots
@rem set REPO_URL=https://maven-play-plugin.googlecode.com/svn/mavenrepo/snapshots

rem call mvn clean source:jar javadoc:jar --file %SRC_DIR%/pom.xml
call mvn deploy:deploy-file -Dfile=%PLAY_HOME%/framework/lib/hibernate-core-3.6.10.Final.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=%SRC_DIR%/pom.xml -Dsources=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-sources.jar -Djavadoc=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-javadoc.jar -DrepositoryId=%REPO_ID% -Durl=dav:%REPO_URL% -e
