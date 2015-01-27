call ..\set-play-home-1.3.0.bat

set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%org.eclipse.jdt
set ARTIFACT_ID=org.eclipse.jdt.core
set VERSION=3.10.0.v20140604-1726

set SRC_DIR=../sources/%ARTIFACT_ID%/%VERSION%/src/org.eclipse.jdt.core

@rem call mvn clean source:jar javadoc:jar --file %SRC_DIR%/pom.xml
call mvn install:install-file -Dfile=%PLAY_HOME%/framework/lib/%ARTIFACT_ID%-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/play/%ARTIFACT_ID%-%VERSION%.pom -Dsources=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-sources.jar -Djavadoc=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-javadoc.jar
@rem call mvn install:install-file -Dfile=%PLAY_HOME%/framework/lib/%ARTIFACT_ID%-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/play/%ARTIFACT_ID%-%VERSION%.pom
@rem call mvn install:install-file -Dfile=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-javadoc.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -Dclassifier=javadoc -DgeneratePom=false
@rem call mvn install:install-file -Dfile=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-sources.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -Dclassifier=sources -DgeneratePom=false
