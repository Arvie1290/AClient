@echo off
:loop
cls
echo =================================
echo AClient Play gradlew runClient
echo =================================
echo.
echo Good luck bro the debugging :3.
echo.

call gradlew.bat runClient

echo.
echo Debugging Done.
echo Press [R] for Restart or [Q] for Quit...

choice /c rq /n

if errorlevel 2 goto end
if errorlevel 1 goto loop

:end
echo.
echo Bye bro.
pause
