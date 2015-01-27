call ..\set-play-home-1.3.0.bat

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%org.apache.commons
set ARTIFACT_ID=commons-javaflow
set BASE_VERSION=1590792
set VERSION=%BASE_VERSION%-patched-play-1.3.0

set REPO_ID=sonatype-nexus-staging
set REPO_URL=https://oss.sonatype.org/service/local/staging/deploy/maven2

set SRC_DIR=../sources/%ARTIFACT_ID%/%VERSION%/trunk.patched-play-1.3.0

call mvn gpg:sign-and-deploy-file -Durl=%REPO_URL% -DrepositoryId=%REPO_ID% -DpomFile=../poms/play/%ARTIFACT_ID%-%VERSION%.pom -Dfile=%PLAY_HOME%/framework/lib/%ARTIFACT_ID%-%BASE_VERSION%.jar -Dclassifiers=sources,javadoc -Dtypes=jar,jar -Dfiles=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-sources.jar,%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-javadoc.jar
