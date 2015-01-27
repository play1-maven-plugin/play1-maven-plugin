set ARTIFACT_ID=commons-javaflow
set VERSION=1590792
set SRC_DIR=trunk.patched-play-1.3.0

call svn export -r %VERSION% http://svn.apache.org/repos/asf/commons/sandbox/javaflow/trunk trunk

xcopy /I /E /Q trunk %SRC_DIR%
rename %SRC_DIR%\pom.xml pom.xml-orig 
copy ..\..\..\poms\play\%ARTIFACT_ID%-1590792-patched-play-1.3.0.pom %SRC_DIR%\pom.xml

rem xcopy /I /E /Q /Y patched-files-1.3.0 %SRC_DIR%
