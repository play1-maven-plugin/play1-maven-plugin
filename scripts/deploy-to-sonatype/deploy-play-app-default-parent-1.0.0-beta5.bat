set GROUP_ID=com.google.code.maven-play-plugin
set ARTIFACT_ID=play-app-default-parent
set VERSION=1.0.0-beta5
set SRC_DIR=../../../tags/play-default-parents-%VERSION%/%ARTIFACT_ID%

set REPO_ID=sonatype-nexus-staging
set REPO_URL=https://oss.sonatype.org/service/local/staging/deploy/maven2
@rem set REPO_ID=sonatype-nexus-snapshots
@rem set REPO_URL=https://oss.sonatype.org/content/repositories/snapshots

call mvn gpg:sign-and-deploy-file -Durl=%REPO_URL% -DrepositoryId=%REPO_ID% -DgeneratePom=false -Dfile=%SRC_DIR%/pom.xml -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=pom -Dversion=%VERSION% -Dgpg.homedir=./.gpg
