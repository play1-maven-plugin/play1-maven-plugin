@rem call install-130-deps

set VERSION=1.3.0
call ..\set-play-home-%VERSION%.bat

call install-130-play
call install-130-play-crud
call install-130-play-docviewer
call install-130-play-grizzly
call install-130-play-secure
call install-130-play-testrunner
