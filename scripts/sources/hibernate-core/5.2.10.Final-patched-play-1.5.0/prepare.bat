call wget --no-check-certificate --output-document=hibernate-orm-5.2.10.zip https://github.com/hibernate/hibernate-orm/archive/5.2.10.zip

call jar xf hibernate-orm-5.2.10.zip

set SRC_DIR=patched-play-1.5.0

move hibernate-orm-5.2.10 %SRC_DIR%

xcopy /I /E /Q /Y patched-files-1.5.0\*.* %SRC_DIR%
