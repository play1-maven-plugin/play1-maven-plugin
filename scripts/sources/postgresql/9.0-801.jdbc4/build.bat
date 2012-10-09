wget http://central.maven.org/maven2/postgresql/postgresql/9.0-801.jdbc4/postgresql-9.0-801.jdbc4-sources.jar
wget http://central.maven.org/maven2/postgresql/postgresql/9.0-801.jdbc4/postgresql-9.0-801.jdbc4-javadoc.jar

copy postgresql-9.0-801.jdbc4-sources.jar postgresql-9.0-801.jdbc4-patched-play-1.2.3-sources.jar
call jar uf postgresql-9.0-801.jdbc4-patched-play-1.2.3-sources.jar -C patched-files-1.2.3 .

copy postgresql-9.0-801.jdbc4-javadoc.jar postgresql-9.0-801.jdbc4-patched-play-1.2.3-javadoc.jar
