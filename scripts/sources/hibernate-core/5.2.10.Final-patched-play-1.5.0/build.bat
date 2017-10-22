call setjava8

set SRC_DIR=patched-play-1.5.0

cd %SRC_DIR%
call gradlew hibernate-core:assemble hibernate-core:sourcesJar hibernate-core:javadocJar > ..\hibernate-core-all.log 2>&1
cd ..
