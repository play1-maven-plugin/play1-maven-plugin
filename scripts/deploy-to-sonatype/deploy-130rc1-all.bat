@rem call deploy-130-deps

set VERSION=1.3.0-RC1
call ..\set-play-home-%VERSION%.bat

call deploy-130rc1-play
call deploy-130rc1-play-crud
call deploy-130rc1-play-docviewer
call deploy-130rc1-play-grizzly
call deploy-130rc1-play-secure
call deploy-130rc1-play-testrunner
