@rem Call it once:
@rem call mvn clean install --file %PLAY_HOME%/parent/pom.xml

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%org.playframework
set ARTIFACT_ID=play
set SRC_DIR=%PLAY_HOME%/framework

@rem set REPO_ID=sonatype-nexus-staging
@rem set REPO_URL=https://oss.sonatype.org/service/local/staging/deploy/maven2
set REPO_ID=sonatype-nexus-snapshots
set REPO_URL=https://oss.sonatype.org/content/repositories/snapshots

@rem call mvn clean package source:jar javadoc:jar --file %SRC_DIR%/pom-build-dist.xml
call mvn clean package source:jar javadoc:jar --file %SRC_DIR%/pom-build-dist.xml -Dmaven.test.skip=true

call mvn gpg:sign-and-deploy-file -Durl=%REPO_URL% -DrepositoryId=%REPO_ID% -DpomFile=../poms/play/%VERSION%/framework/pom-dist.xml -Dfile=%SRC_DIR%/play-1.2.x-localbuild.jar  -Dclassifiers=sources,javadoc,framework-min,framework -Dtypes=jar,jar,zip,zip -Dfiles=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-sources.jar,%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-javadoc.jar,%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-framework-min.zip,%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-framework.zip
