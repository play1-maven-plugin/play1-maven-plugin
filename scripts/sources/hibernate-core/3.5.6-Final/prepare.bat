call svn export http://anonsvn.jboss.org/repos/hibernate/core/tags/hibernate-3.5.6-Final/core

set SRC_DIR=core.patched-play-1.1
xcopy /I /E /Q core %SRC_DIR%
copy ..\..\..\poms\play\hibernate-core-3.5.6-Final-patched-play-1.1.pom %SRC_DIR%\pom.xml
xcopy /I /E /Q /Y patched-files-1.1 %SRC_DIR%
call mvn clean source:jar javadoc:jar --file %SRC_DIR%/pom.xml

set SRC_DIR=core.patched-play-1.1.1
xcopy /I /E /Q core %SRC_DIR%
copy ..\..\..\poms\play\hibernate-core-3.5.6-Final-patched-play-1.1.1.pom %SRC_DIR%\pom.xml
xcopy /I /E /Q /Y patched-files-1.1.1 %SRC_DIR%
call mvn clean source:jar javadoc:jar --file %SRC_DIR%/pom.xml
