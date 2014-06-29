set GROUP_ID=com.google.code.maven-play-plugin
set ARTIFACT_ID=play13-app-default-parent
set VERSION=1.0.0-beta7

set REPO_ID=sonatype-nexus-staging
set REPO_URL=https://oss.sonatype.org/service/local/staging/deploy/maven2

call mvn gpg:sign-and-deploy-file -Durl=%REPO_URL% -DrepositoryId=%REPO_ID% -DgeneratePom=false -Dfile=../poms/%SRC_DIR%/%ARTIFACT_ID%-%VERSION%.pom -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=pom -Dversion=%VERSION%
