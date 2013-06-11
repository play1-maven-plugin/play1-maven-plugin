@rem call install-126snapshot-deps

set VERSION=1.2.6-SNAPSHOT
call ..\set-play-home-%VERSION%.bat

call mvn clean install --file %PLAY_HOME%/parent/pom.xml
call mvn clean install --file %PLAY_HOME%/modules/parent/pom.xml

call install-126snapshot-play
call install-126snapshot-play-crud
call install-126snapshot-play-docviewer
call install-126snapshot-play-grizzly
call install-126snapshot-play-secure
call install-126snapshot-play-testrunner
