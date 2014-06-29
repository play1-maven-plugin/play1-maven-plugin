call ..\set-play-home-1.3.0-RC1.bat

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%org.eclipse.jdt
set ARTIFACT_ID=org.eclipse.jdt.core
set VERSION=3.9.50.v20140317-1741

set REPO_ID=sonatype-nexus-staging
set REPO_URL=https://oss.sonatype.org/service/local/staging/deploy/maven2

set SRC_DIR=../sources/org.eclipse.jdt.core/%VERSION%/src/org.eclipse.jdt.core

call mvn gpg:sign-and-deploy-file -Durl=%REPO_URL% -DrepositoryId=%REPO_ID% -DpomFile=../poms/play/%ARTIFACT_ID%-%VERSION%.pom -Dfile=%PLAY_HOME%/framework/lib/%ARTIFACT_ID%-%VERSION%.jar -Dclassifiers=sources,javadoc -Dtypes=jar,jar -Dfiles=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-sources.jar,%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-javadoc.jar
