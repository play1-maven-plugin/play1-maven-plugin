@rem call install-130snapshot-deps

set VERSION=1.3.0-SNAPSHOT
call ..\set-play-home-%VERSION%.bat

call mvn clean install --file %PLAY_HOME%/parent/pom.xml
call mvn clean install --file %PLAY_HOME%/modules/parent/pom.xml

@rem call install-130snapshot-deps
@rem call install-130snapshot-play
@rem call install-130snapshot-play-crud
@rem call install-130snapshot-play-docviewer
@rem call install-130snapshot-play-grizzly
@rem call install-130snapshot-play-secure
@rem call install-130snapshot-play-testrunner
