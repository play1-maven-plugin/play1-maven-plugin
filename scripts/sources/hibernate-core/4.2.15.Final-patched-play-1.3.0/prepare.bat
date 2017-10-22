call wget --no-check-certificate --output-document=hibernate-orm-4.2.15.Final.zip https://github.com/hibernate/hibernate-orm/archive/4.2.15.Final.zip

call jar xf hibernate-orm-4.2.15.Final.zip

set SRC_DIR=patched-play-1.3.0
mkdir %SRC_DIR%

xcopy /I /E /Q hibernate-orm-4.2.15.Final\*.* %SRC_DIR%
xcopy /I /E /Q /Y patched-files-1.3.0\*.* %SRC_DIR%