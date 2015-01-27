set SRC_DIR=trunk.patched-play-1.3.0

call mvn clean source:jar javadoc:jar --file %SRC_DIR%/pom.xml
