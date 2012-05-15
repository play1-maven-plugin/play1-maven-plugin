call ..\set-external-modules-home.bat
set MODULE_NAME=japid
set MODULE_VERSION=0.9.3.4
set SRC_DIR=%MODULES_HOME%/%MODULE_NAME%-%MODULE_VERSION%/lib.plain

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%org.eclipse.jdt
set ARTIFACT_ID=org.eclipse.jdt.core
set VERSION=3.8.0.v_C18

set REPO_ID=com.google.code.maven-play-plugin
set REPO_URL=https://maven-play-plugin.googlecode.com/svn/mavenrepo/releases
@rem set REPO_ID=com.google.code.maven-play-plugin-snapshots
@rem set REPO_URL=https://maven-play-plugin.googlecode.com/svn/mavenrepo/snapshots

@rem call mvn deploy:deploy-file -Dfile=../poms/modules/japid/%ARTIFACT_ID%-%VERSION%.pom -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=pom -Dversion=%VERSION% -DgeneratePom=false -DrepositoryId=%REPO_ID% -Durl=dav:%REPO_URL% -e
call mvn deploy:deploy-file -Dfile=%SRC_DIR%/%ARTIFACT_ID%-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/modules/japid/%ARTIFACT_ID%-%VERSION%.pom -DrepositoryId=%REPO_ID% -Durl=dav:%REPO_URL% -e
