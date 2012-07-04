set ARTIFACT_ID=commons-javaflow
set SRC_DIR=trunk-rel.%VERSION%

del /Q /S %SRC_DIR%
call svn export -r %VERSION% http://svn.apache.org/repos/asf/commons/sandbox/javaflow/trunk %SRC_DIR%
rename %SRC_DIR%\pom.xml pom.xml-orig 
copy ..\..\poms\play\%ARTIFACT_ID%-%VERSION%.pom %SRC_DIR%\pom.xml
