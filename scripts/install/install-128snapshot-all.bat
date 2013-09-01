@rem call install-128snapshot-deps

set VERSION=1.2.8-SNAPSHOT
call ..\set-play-home-%VERSION%.bat

call install-128snapshot-play
call install-128snapshot-play-crud
call install-128snapshot-play-docviewer
call install-128snapshot-play-grizzly
call install-128snapshot-play-secure
call install-128snapshot-play-testrunner
