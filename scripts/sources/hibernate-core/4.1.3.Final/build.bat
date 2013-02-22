set SRC_DIR=patched-play-1.3.0-SNAPSHOT

call setjava6
cd %SRC_DIR%
call gradlew hibernate-core:assemble hibernate-core:javadocJar > hibernate-core-all.log 2>&1
cd ..
