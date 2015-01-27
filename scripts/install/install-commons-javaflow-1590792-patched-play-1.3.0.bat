call ..\set-play-home-1.3.0.bat

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%org.apache.commons
set ARTIFACT_ID=commons-javaflow
set BASE_VERSION=1590792
set VERSION=%BASE_VERSION%-patched-play-1.3.0

set SRC_DIR=../sources/%ARTIFACT_ID%/%VERSION%/trunk.patched-play-1.3.0

rem call mvn clean source:jar javadoc:jar --file %SRC_DIR%/pom.xml
call mvn install:install-file -Dfile=%PLAY_HOME%/framework/lib/%ARTIFACT_ID%-%BASE_VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/play/%ARTIFACT_ID%-%VERSION%.pom -Dsources=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-sources.jar -Djavadoc=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-javadoc.jar
rem call mvn install:install-file -Dfile=%PLAY_HOME%/framework/lib/%ARTIFACT_ID%-%BASE_VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/play/%ARTIFACT_ID%-%VERSION%.pom
rem call mvn install:install-file -Dfile=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-javadoc.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -Dclassifier=javadoc -DgeneratePom=false
rem call mvn install:install-file -Dfile=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-sources.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -Dclassifier=sources -DgeneratePom=false
