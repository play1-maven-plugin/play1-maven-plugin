@rem call install-130snapshot-deps

set VERSION=1.3.0-SNAPSHOT
call ..\set-play-home-%VERSION%.bat

call install-130snapshot-deps
call install-130snapshot-play
call install-130snapshot-play-crud
call install-130snapshot-play-docviewer
call install-130snapshot-play-grizzly
call install-130snapshot-play-secure
call install-130snapshot-play-testrunner
