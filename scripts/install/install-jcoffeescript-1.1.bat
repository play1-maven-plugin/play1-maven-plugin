call ..\set-external-modules-home.bat
set MODULE_NAME=greenscript
@rem set MODULE_VERSION=1.2.8
@rem set SRC_DIR=%MODULES_HOME%/%MODULE_NAME%-%MODULE_VERSION%/lib
set SRC_DIR=I:/scm/github.git/gslowikowski/jcoffeescript/trunk

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%com.github.yeungda.jcoffeescript
set ARTIFACT_ID=jcoffeescript
set ARTIFACT_ID_DEP=jcoffeescript-dep
set VERSION=1.1

call mvn clean source:jar javadoc:jar --file %SRC_DIR%/pom.xml
call mvn install:install-file -Dfile=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID%-%VERSION%.pom -Dsources=%SRC_DIR%/target/%ARTIFACT_ID_DEP%-%VERSION%-sources.jar -Djavadoc=%SRC_DIR%/target/%ARTIFACT_ID_DEP%-%VERSION%-javadoc.jar
call mvn install:install-file -Dfile=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID_DEP%-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID_DEP% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/modules/%MODULE_NAME%/%ARTIFACT_ID_DEP%-%VERSION%.pom -Dsources=%SRC_DIR%/target/%ARTIFACT_ID_DEP%-%VERSION%-sources.jar -Djavadoc=%SRC_DIR%/target/%ARTIFACT_ID_DEP%-%VERSION%-javadoc.jar
