call ..\set-external-modules-home.bat
set MODULE_NAME=scala
set MODULE_VERSION=0.9.1
set SRC_DIR=%MODULES_HOME%/%MODULE_NAME%-%MODULE_VERSION%/lib

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%org.scalatest
set ARTIFACT_ID=scalatest
set VERSION=1.2-for-scala-2.8.0.RC7-20100708

set REPO_ID=com.google.code.maven-play-plugin
set REPO_URL=https://maven-play-plugin.googlecode.com/svn/mavenrepo/releases
@rem set REPO_ID=com.google.code.maven-play-plugin-snapshots
@rem set REPO_URL=https://maven-play-plugin.googlecode.com/svn/mavenrepo/snapshots

call mvn deploy:deploy-file -Dfile=%SRC_DIR%/%ARTIFACT_ID%-1.2-for-scala-2.8.0.RC7-SNAPSHOT.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%.pom -Dsources=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%-sources.jar -Djavadoc=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%-javadoc.jar -Dfiles=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%-scaladoc.jar -Dtypes=jar -Dclassifiers=scaladoc -DrepositoryId=%REPO_ID% -Durl=dav:%REPO_URL% -e
