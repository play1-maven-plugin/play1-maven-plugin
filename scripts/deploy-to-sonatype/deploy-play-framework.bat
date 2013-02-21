set GROUP_PATH=com/google/code/maven-play-plugin/org/playframework
set ARTIFACT_ID=play

@rem set LOCAL_REPO_PATH=../../../mavenrepo/releases
set LOCAL_REPO_PATH=I:/scm/googlecode.svn/maven-play-plugin/mavenrepo/releases
set REPO_ID=sonatype-nexus-staging
set REPO_URL=https://oss.sonatype.org/service/local/staging/deploy/maven2

set POM_PATH=../poms/play/%VERSION%/framework/pom-dist-central.xml
set FILE_BASE_PATH=%LOCAL_REPO_PATH%/%GROUP_PATH%/%ARTIFACT_ID%/%VERSION%/%ARTIFACT_ID%-%VERSION%

call mvn -X gpg:sign-and-deploy-file -Durl=%REPO_URL% -DrepositoryId=%REPO_ID% -DpomFile=%POM_PATH% -Dfile=%FILE_BASE_PATH%.jar  -Dclassifiers=sources,javadoc,framework-min,framework -Dtypes=jar,jar,zip,zip -Dfiles=%FILE_BASE_PATH%-sources.jar,%FILE_BASE_PATH%-javadoc.jar,%FILE_BASE_PATH%-framework-min.zip,%FILE_BASE_PATH%-framework.zip

@rem file by file
@rem call mvn gpg:sign-and-deploy-file -Durl=%REPO_URL% -DrepositoryId=%REPO_ID% -DpomFile=%POM_PATH% -Dfile=%FILE_BASE_PATH%.jar
@rem call mvn gpg:sign-and-deploy-file -Durl=%REPO_URL% -DrepositoryId=%REPO_ID% -DpomFile=%POM_PATH% -Dfile=%FILE_BASE_PATH%-sources.jar -Dclassifier=sources
@rem call mvn gpg:sign-and-deploy-file -Durl=%REPO_URL% -DrepositoryId=%REPO_ID% -DpomFile=%POM_PATH% -Dfile=%FILE_BASE_PATH%-javadoc.jar -Dclassifier=javadoc
@rem call mvn gpg:sign-and-deploy-file -Durl=%REPO_URL% -DrepositoryId=%REPO_ID% -DpomFile=%POM_PATH% -Dfile=%FILE_BASE_PATH%-framework-min.zip -Dclassifier=framework-min -Dpackaging=zip
@rem call mvn gpg:sign-and-deploy-file -Durl=%REPO_URL% -DrepositoryId=%REPO_ID% -DpomFile=%POM_PATH% -Dfile=%FILE_BASE_PATH%-framework.zip -Dclassifier=framework -Dpackaging=zip
