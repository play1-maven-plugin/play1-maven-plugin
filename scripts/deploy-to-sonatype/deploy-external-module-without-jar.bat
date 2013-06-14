set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%org.playframework.modules.%MODULE_NAME%
if "%ARTIFACT_ID%"=="" set ARTIFACT_ID=play-%MODULE_NAME%
set SRC_DIR=%MODULES_HOME%/%MODULE_NAME%-%VERSION%

set REPO_ID=com.google.code.maven-play-plugin
set REPO_URL=https://maven-play-plugin.googlecode.com/svn/mavenrepo/releases
@rem set REPO_ID=com.google.code.maven-play-plugin-snapshots
@rem set REPO_URL=https://maven-play-plugin.googlecode.com/svn/mavenrepo/snapshots

call mvn clean package -Pdist --file %SRC_DIR%/pom-build-dist.xml
call mvn deploy:deploy-file -Dfile=%SRC_DIR%/pom-dist.xml -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=pom -Dversion=%VERSION% -DgeneratePom=false -Dfiles=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-module.zip,%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-module-min.zip -Dtypes=zip,zip -Dclassifiers=module,module-min -DrepositoryId=%REPO_ID% -Durl=dav:%REPO_URL% -e
