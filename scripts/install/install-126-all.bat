@rem call install-126-deps

set VERSION=1.2.6
call ..\set-play-home-%VERSION%.bat

call install-126-play
call install-126-play-crud
call install-126-play-docviewer
call install-126-play-grizzly
call install-126-play-secure
call install-126-play-testrunner
