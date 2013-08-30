@rem call install-127-deps

set VERSION=1.2.7
call ..\set-play-home-%VERSION%.bat

call install-127-play
call install-127-play-crud
call install-127-play-docviewer
call install-127-play-grizzly
call install-127-play-secure
call install-127-play-testrunner
