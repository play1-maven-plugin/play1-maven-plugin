setlocal

set SRC_DIR=%1
set DST_DIR=%2

copy %SRC_DIR%\pom.xml %DST_DIR%\pom.xml
copy %SRC_DIR%\distribution\assembly-distribution.xml %DST_DIR%\distribution\assembly-distribution.xml
copy %SRC_DIR%\distribution\pom.xml %DST_DIR%\distribution\pom.xml
copy %SRC_DIR%\framework\assembly-framework.xml %DST_DIR%\framework\assembly-framework.xml
copy %SRC_DIR%\framework\assembly-framework-build-dist.xml %DST_DIR%\framework\assembly-framework-build-dist.xml
copy %SRC_DIR%\framework\assembly-framework-min.xml %DST_DIR%\framework\assembly-framework-min.xml
copy %SRC_DIR%\framework\pom.xml %DST_DIR%\framework\pom.xml
copy %SRC_DIR%\framework\pom-dist.xml %DST_DIR%\framework\pom-dist.xml
copy %SRC_DIR%\framework\pom-build-dist.xml %DST_DIR%\framework\pom-build-dist.xml
copy %SRC_DIR%\modules\pom.xml %DST_DIR%\modules\pom.xml
copy %SRC_DIR%\modules\crud\assembly-module.xml %DST_DIR%\modules\crud\assembly-module.xml
copy %SRC_DIR%\modules\crud\assembly-module-min.xml %DST_DIR%\modules\crud\assembly-module-min.xml
copy %SRC_DIR%\modules\crud\pom.xml %DST_DIR%\modules\crud\pom.xml
copy %SRC_DIR%\modules\crud\pom-dist.xml %DST_DIR%\modules\crud\pom-dist.xml
copy %SRC_DIR%\modules\crud\pom-build-dist.xml %DST_DIR%\modules\crud\pom-build-dist.xml
copy %SRC_DIR%\modules\docviewer\assembly-module.xml %DST_DIR%\modules\docviewer\assembly-module.xml
copy %SRC_DIR%\modules\docviewer\assembly-module-min.xml %DST_DIR%\modules\docviewer\assembly-module-min.xml
copy %SRC_DIR%\modules\docviewer\pom.xml %DST_DIR%\modules\docviewer\pom.xml
copy %SRC_DIR%\modules\docviewer\pom-dist.xml %DST_DIR%\modules\docviewer\pom-dist.xml
copy %SRC_DIR%\modules\docviewer\pom-build-dist.xml %DST_DIR%\modules\docviewer\pom-build-dist.xml
copy %SRC_DIR%\modules\grizzly\assembly-module.xml %DST_DIR%\modules\grizzly\assembly-module.xml
copy %SRC_DIR%\modules\grizzly\assembly-module-build-dist.xml %DST_DIR%\modules\grizzly\assembly-module-build-dist.xml
copy %SRC_DIR%\modules\grizzly\assembly-module-min.xml %DST_DIR%\modules\grizzly\assembly-module-min.xml
copy %SRC_DIR%\modules\grizzly\no-content.txt %DST_DIR%\modules\grizzly\no-content.txt
copy %SRC_DIR%\modules\grizzly\pom.xml %DST_DIR%\modules\grizzly\pom.xml
copy %SRC_DIR%\modules\grizzly\pom-build-dist.xml %DST_DIR%\modules\grizzly\pom-build-dist.xml
copy %SRC_DIR%\modules\grizzly\pom-dist.xml %DST_DIR%\modules\grizzly\pom-dist.xml
copy %SRC_DIR%\modules\parent\pom.xml %DST_DIR%\modules\parent\pom.xml
copy %SRC_DIR%\modules\secure\assembly-module.xml %DST_DIR%\modules\secure\assembly-module.xml
copy %SRC_DIR%\modules\secure\assembly-module-min.xml %DST_DIR%\modules\secure\assembly-module-min.xml
copy %SRC_DIR%\modules\secure\pom.xml %DST_DIR%\modules\secure\pom.xml
copy %SRC_DIR%\modules\secure\pom-dist.xml %DST_DIR%\modules\secure\pom-dist.xml
copy %SRC_DIR%\modules\secure\pom-build-dist.xml %DST_DIR%\modules\secure\pom-build-dist.xml
copy %SRC_DIR%\modules\testrunner\assembly-module.xml %DST_DIR%\modules\testrunner\assembly-module.xml
copy %SRC_DIR%\modules\testrunner\assembly-module-build-dist.xml %DST_DIR%\modules\testrunner\assembly-module-build-dist.xml
copy %SRC_DIR%\modules\testrunner\assembly-module-min.xml %DST_DIR%\modules\testrunner\assembly-module-min.xml
copy %SRC_DIR%\modules\testrunner\pom.xml %DST_DIR%\modules\testrunner\pom.xml
copy %SRC_DIR%\modules\testrunner\pom-dist.xml %DST_DIR%\modules\testrunner\pom-dist.xml
copy %SRC_DIR%\modules\testrunner\pom-build-dist.xml %DST_DIR%\modules\testrunner\pom-build-dist.xml
copy %SRC_DIR%\parent\pom.xml %DST_DIR%\parent\pom.xml

endlocal
