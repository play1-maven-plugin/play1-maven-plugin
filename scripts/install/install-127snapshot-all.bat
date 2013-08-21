@rem call install-127snapshot-deps

set VERSION=1.2.7-SNAPSHOT
call ..\set-play-home-%VERSION%.bat

call install-127snapshot-play
call install-127snapshot-play-crud
call install-127snapshot-play-docviewer
call install-127snapshot-play-grizzly
call install-127snapshot-play-secure
call install-127snapshot-play-testrunner
