set GROUP_ID_PREFIX=com.google.code.maven-play-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%org.playframework
set ARTIFACT_ID=play
set SRC_DIR=%PLAY_HOME%/framework

call mvn clean package source:jar javadoc:jar -Pdist --file %SRC_DIR%/pom-build-dist.xml

call mvn install:install-file -Dfile=%SRC_DIR%/play-1.2.x-localbuild.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/play/%VERSION%/framework/pom-dist.xml -Dsources=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-sources.jar -Djavadoc=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-javadoc.jar
call mvn install:install-file -Dfile=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-framework.zip -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=zip -Dversion=%VERSION% -Dclassifier=framework -DgeneratePom=false
call mvn install:install-file -Dfile=%SRC_DIR%/target/%ARTIFACT_ID%-%VERSION%-framework-min.zip -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=zip -Dversion=%VERSION% -Dclassifier=framework-min -DgeneratePom=false
