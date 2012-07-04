@rem installed when installing framework
@rem call mvn clean install --file %PLAY_HOME%/parent/pom.xml
@rem call mvn clean install --file %PLAY_HOME%/modules/parent/pom.xml

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%org.playframework.modules.%MODULE_NAME%
set ARTIFACT_ID=play-%MODULE_NAME%
set SRC_DIR=%PLAY_HOME%/modules/%MODULE_NAME%

call mvn clean package --file %SRC_DIR%/pom-build-dist.xml
call mvn install:install-file -Dfile=%SRC_DIR%/pom-dist.xml -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=pom -Dversion=%VERSION% -DgeneratePom=false
call mvn install:install-file -Dfile=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-module.zip -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=zip -Dversion=%VERSION% -Dclassifier=module -DgeneratePom=false
call mvn install:install-file -Dfile=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-module-min.zip -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=zip -Dversion=%VERSION% -Dclassifier=module-min -DgeneratePom=false
