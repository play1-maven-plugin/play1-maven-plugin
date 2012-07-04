setlocal 

call ..\set-play-home-1.1.bat
call copy-framework.bat play\1.1 %PLAY_HOME%

call ..\set-play-home-1.1.1.bat
call copy-framework.bat play\1.1.1 %PLAY_HOME%

call ..\set-play-home-1.1.2.bat
call copy-framework.bat play\1.1.2 %PLAY_HOME%

call ..\set-play-home-1.2.bat
call copy-framework.bat play\1.2 %PLAY_HOME%

call ..\set-play-home-1.2.1.bat
call copy-framework.bat play\1.2.1 %PLAY_HOME%

call ..\set-play-home-1.2.1.1.bat
call copy-framework.bat play\1.2.1.1 %PLAY_HOME%

call ..\set-play-home-1.2.2.bat
call copy-framework.bat play\1.2.2 %PLAY_HOME%

call ..\set-play-home-1.2.3.bat
call copy-framework.bat play\1.2.3 %PLAY_HOME%

call ..\set-play-home-1.2.4.bat
call copy-framework.bat play\1.2.4 %PLAY_HOME%

call ..\set-play-home-1.2.5.bat
call copy-framework.bat play\1.2.5 %PLAY_HOME%

endlocal
