call svn export -r 1066591 http://svn.apache.org/repos/asf/commons/sandbox/javaflow/trunk

set SRC_DIR=trunk-rel.1066591
xcopy /I /E /Q trunk %SRC_DIR%
copy ..\..\poms\play\commons-javaflow-1066591.pom %SRC_DIR%\pom.xml
call mvn clean source:jar javadoc:jar --file %SRC_DIR%/pom.xml
