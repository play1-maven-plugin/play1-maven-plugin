set GROUP_ID=com.google.code.maven-play-plugin
set ARTIFACT_ID=play-app-default-parent
set VERSION=1.0.0-beta2
set SRC_DIR=../../../tags/play-default-parents-%VERSION%/%ARTIFACT_ID%

set REPO_ID=com.google.code.maven-play-plugin
set REPO_URL=https://maven-play-plugin.googlecode.com/svn/mavenrepo/releases
@rem set REPO_ID=com.google.code.maven-play-plugin-snapshots
@rem set REPO_URL=https://maven-play-plugin.googlecode.com/svn/mavenrepo/snapshots

call mvn deploy:deploy-file -Dfile=%SRC_DIR%/pom.xml -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=pom -Dversion=%VERSION% -DgeneratePom=false -DrepositoryId=%REPO_ID% -Durl=dav:%REPO_URL% -e
