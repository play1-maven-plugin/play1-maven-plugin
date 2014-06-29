@rem call install-130-deps

set VERSION=1.3.0-RC1
call ..\set-play-home-%VERSION%.bat

call install-130rc1-play
call install-130rc1-play-crud
call install-130rc1-play-docviewer
call install-130rc1-play-grizzly
call install-130rc1-play-secure
call install-130rc1-play-testrunner
