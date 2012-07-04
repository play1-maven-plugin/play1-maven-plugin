call mvn clean install --file %PLAY_HOME%/parent/pom.xml
call mvn clean install --file %PLAY_HOME%/modules/parent/pom.xml

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%org.playframework
set ARTIFACT_ID=play
set SRC_DIR=%PLAY_HOME%/framework

set REPO_ID=com.google.code.maven-play-plugin
set REPO_URL=https://maven-play-plugin.googlecode.com/svn/mavenrepo/releases
@rem set REPO_ID=com.google.code.maven-play-plugin-snapshots
@rem set REPO_URL=https://maven-play-plugin.googlecode.com/svn/mavenrepo/snapshots

call mvn clean package source:jar javadoc:jar --file %SRC_DIR%/pom-build-dist.xml -Dmaven.test.skip=true
call mvn deploy:deploy-file -Dfile=%SRC_DIR%/%ARTIFACT_ID%-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=%SRC_DIR%/pom-dist.xml -Dsources=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-sources.jar -Djavadoc=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-javadoc.jar -Dfiles=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-framework.zip,%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-framework-min.zip -Dtypes=zip,zip -Dclassifiers=framework,framework-min -DrepositoryId=%REPO_ID% -Durl=dav:%REPO_URL% -e

rem call mvn package --file %SRC_DIR%/pom-build-dist.xml
rem call mvn deploy:deploy-file -Dfile=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-framework-min.zip -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dclassifier=framework-min -Dpackaging=zip -Dversion=%VERSION% -DgeneratePom=false -DrepositoryId=%REPO_ID% -Durl=dav:%REPO_URL% -e
