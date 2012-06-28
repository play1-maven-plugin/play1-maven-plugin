call ..\set-external-modules-home.bat
set MODULE_NAME=rythm
set MODULE_VERSION=1.0.0-RC8
set SRC_DIR=../sources/rythm/1.0.0-20120628

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%com.rythmengine
set ARTIFACT_ID=rythm
set VERSION=1.0.0-20120628

set REPO_ID=com.google.code.maven-play-plugin
set REPO_URL=https://maven-play-plugin.googlecode.com/svn/mavenrepo/releases
@rem set REPO_ID=com.google.code.maven-play-plugin-snapshots
@rem set REPO_URL=https://maven-play-plugin.googlecode.com/svn/mavenrepo/snapshots

call mvn clean package source:jar javadoc:jar --file %SRC_DIR%/pom.xml
call mvn deploy:deploy-file -Dfile=%MODULES_HOME%/%MODULE_NAME%-%MODULE_VERSION%/lib/%ARTIFACT_ID%-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%.pom -Dsources=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-sources.jar -Djavadoc=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-javadoc.jar -DrepositoryId=%REPO_ID% -Durl=dav:%REPO_URL% -e
