call ..\set-external-modules-home.bat
set MODULE_NAME=less
set MODULE_VERSION=0.9.1
set SRC_DIR=../sources/lesscss-engine/1.3.0-20120408

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%com.asual.lesscss
set ARTIFACT_ID=lesscss-engine
set VERSION=1.3.0-20120408

set REPO_ID=com.google.code.maven-play-plugin
set REPO_URL=https://maven-play-plugin.googlecode.com/svn/mavenrepo/releases
@rem set REPO_ID=com.google.code.maven-play-plugin-snapshots
@rem set REPO_URL=https://maven-play-plugin.googlecode.com/svn/mavenrepo/snapshots

call mvn clean package source:jar javadoc:jar --file %SRC_DIR%/pom.xml
call mvn deploy:deploy-file -Dfile=%MODULES_HOME%/%MODULE_NAME%-%MODULE_VERSION%/lib/%ARTIFACT_ID%-1.3.0.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%.pom -Dsources=%SRC_DIR%/target/%ARTIFACT_ID%-1.3.0-sources.jar -Djavadoc=%SRC_DIR%/target/%ARTIFACT_ID%-1.3.0-javadoc.jar -DrepositoryId=%REPO_ID% -Durl=dav:%REPO_URL% -e
