call ..\set-external-modules-home.bat
set MODULE_NAME=webdrive
set VERSION=0.2

@rem call deploy-external-module-with-jar-without-min.bat
@rem nonstandard final jar name - webdrive-0.2.jar instead of standard play-webdrive.jar
@rem below is modified content of deploy-external-module-with-jar-without-min.bat file:

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%org.playframework.modules.%MODULE_NAME%
set ARTIFACT_ID=play-%MODULE_NAME%
set SRC_DIR=%MODULES_HOME%/%MODULE_NAME%-%VERSION%

set REPO_ID=com.google.code.maven-play-plugin
set REPO_URL=https://maven-play-plugin.googlecode.com/svn/mavenrepo/releases
@rem set REPO_ID=com.google.code.maven-play-plugin-snapshots
@rem set REPO_URL=https://maven-play-plugin.googlecode.com/svn/mavenrepo/snapshots

call mvn clean package source:jar javadoc:jar --file %SRC_DIR%/pom-build-dist.xml
call mvn deploy:deploy-file -Dfile=%SRC_DIR%/lib/%MODULE_NAME%-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=%SRC_DIR%/pom-dist.xml -Dsources=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-sources.jar -Djavadoc=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-javadoc.jar -Dfiles=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-module.zip -Dtypes=zip -Dclassifiers=module -DrepositoryId=%REPO_ID% -Durl=dav:%REPO_URL% -e

rem call mvn deploy:deploy-file -Dfile=%SRC_DIR%/lib/%MODULE_NAME%-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=%SRC_DIR%/pom-dist.xml -DrepositoryId=%REPO_ID% -Durl=dav:%REPO_URL% -e
