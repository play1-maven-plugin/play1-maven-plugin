call ..\set-external-modules-home.bat
set MODULE_NAME=japid
set VERSION=0.9.4.2
set SRC_DIR=%MODULES_HOME%/%MODULE_NAME%-%VERSION%

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%com.github.branaway.japid
set ARTIFACT_ID=%MODULE_NAME%

set REPO_ID=com.google.code.maven-play-plugin
set REPO_URL=https://maven-play-plugin.googlecode.com/svn/mavenrepo/releases
@rem set REPO_ID=com.google.code.maven-play-plugin-snapshots
@rem set REPO_URL=https://maven-play-plugin.googlecode.com/svn/mavenrepo/snapshots

call mvn clean package source:jar javadoc:jar --file %SRC_DIR%/%ARTIFACT_ID%-pom.xml
call mvn deploy:deploy-file -Dfile=%SRC_DIR%/lib/%ARTIFACT_ID%-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=%SRC_DIR%/%ARTIFACT_ID%-pom-dist.xml -Dsources=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-sources.jar -Djavadoc=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-javadoc.jar -DrepositoryId=%REPO_ID% -Durl=dav:%REPO_URL% -e

set GROUP_ID=%GROUP_ID_PREFIX%org.playframework.modules.%MODULE_NAME%
set ARTIFACT_ID=play-%MODULE_NAME%
set JAR_FILE_NAME=%MODULE_NAME%play-%VERSION%
call deploy-external-module-with-jar.bat

