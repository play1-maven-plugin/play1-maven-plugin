set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%org.playframework.modules.%MODULE_NAME%
set ARTIFACT_ID=play-%MODULE_NAME%
set SRC_DIR=%MODULES_HOME%/%MODULE_NAME%-%VERSION%
if "%JAR_FILE_NAME%"=="" set JAR_FILE_NAME=%ARTIFACT_ID%

call mvn clean package source:jar javadoc:jar -Pdist --file %SRC_DIR%/pom-build-dist.xml
call mvn install:install-file -Dfile=%SRC_DIR%/lib/%JAR_FILE_NAME%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=%SRC_DIR%/pom-dist.xml -Dsources=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-sources.jar -Djavadoc=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-javadoc.jar
call mvn install:install-file -Dfile=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-module.zip -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=zip -Dversion=%VERSION% -Dclassifier=module -DgeneratePom=false
call mvn install:install-file -Dfile=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-module-min.zip -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=zip -Dversion=%VERSION% -Dclassifier=module-min -DgeneratePom=false
