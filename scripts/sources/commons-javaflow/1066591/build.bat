set ARTIFACT_ID=commons-javaflow
set VERSION=1066591
set SRC_DIR=trunk.patched-play-1.2

call svn export -r %VERSION% http://svn.apache.org/repos/asf/commons/sandbox/javaflow/trunk trunk

xcopy /I /E /Q trunk %SRC_DIR%
rename %SRC_DIR%\pom.xml pom.xml-orig 
copy ..\..\..\poms\play\%ARTIFACT_ID%-%VERSION%.pom %SRC_DIR%\pom.xml

del /Q %SRC_DIR%\src\main\java\org\apache\commons\javaflow\ContinuationClassLoader.java
del /Q /S %SRC_DIR%\src\main\java\org\apache\commons\javaflow\ant
del /Q %SRC_DIR%\src\main\java\org\apache\commons\javaflow\bytecode\BytecodeClassLoader.java
del /Q /S %SRC_DIR%\src\main\java\org\apache\commons\javaflow\bytecode\transformation\bcel
del /Q /S %SRC_DIR%\src\main\java\org\apache\commons\javaflow\stores
del /Q %SRC_DIR%\src\main\java\org\apache\commons\javaflow\utils\RewritingUtils.java
xcopy /I /E /Q /Y patched-files-1.2 %SRC_DIR%\src\main\java

call mvn clean source:jar javadoc:jar --file %SRC_DIR%/pom.xml
