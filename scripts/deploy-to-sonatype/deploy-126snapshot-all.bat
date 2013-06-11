set VERSION=1.2.6-SNAPSHOT
call ..\set-play-home-%VERSION%.bat

call mvn clean install --file %PLAY_HOME%/parent/pom.xml
call mvn clean install --file %PLAY_HOME%/modules/parent/pom.xml

@rem call deploy-126snapshot-deps
@rem call deploy-126snapshot-play
@rem call deploy-126snapshot-play-crud
@rem call deploy-126snapshot-play-docviewer
@rem call deploy-126snapshot-play-grizzly
@rem call deploy-126snapshot-play-secure
@rem call deploy-126snapshot-play-testrunner
