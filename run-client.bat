@echo off
setlocal

REM ---------------------------------------------------------------------------
REM  Twilight Forest -- build the mod and start a playable test client.
REM
REM  Double-click it, or from a terminal:
REM      run-client.bat            build, then launch
REM      run-client.bat nobuild    launch straight away (nothing changed since
REM                                the last run -- saves the build wait)
REM
REM  The world lives in run\ and persists between launches, so your test world,
REM  options and screenshots are still there next time.
REM
REM  To reach the Twilight Forest once you are in game, either:
REM    * build a mossy cobblestone frame and light it, or
REM    * set MUSHROOM_PORTAL = true in run\config\twilightforest.cfg and hold a
REM      red mushroom -- upstream 1.p4's own mechanism, off by default.
REM ---------------------------------------------------------------------------

REM Every call below uses an EXPLICIT "%~dp0..." path, and that is not style.
REM Where NoDefaultCurrentDirectoryInExePath is set, cmd does not search the
REM current directory when resolving a command name -- so a bare
REM `call gradlew.bat` fails with "not recognized" even while `if exist` finds
REM the file sitting right there. Do not "tidy" these back into bare names.
set "GRADLEW=%~dp0gradlew.bat"

REM The trailing dot keeps cmd from eating the closing quote: %~dp0 already ends
REM in a backslash, and "...\" escapes it.
cd /d "%~dp0."

if not exist "%GRADLEW%" (
    echo.
    echo   ERROR: gradlew.bat is not next to this script.
    echo   run-client.bat has to sit in the repository root.
    echo.
    pause
    exit /b 1
)

if /i "%~1"=="nobuild" goto :launch

echo.
echo === Building Twilight Forest ===
echo.

REM `call` matters: without it, control never returns from gradlew.bat and
REM everything below this line is silently skipped.
call "%GRADLEW%" build
if errorlevel 1 (
    echo.
    echo   BUILD FAILED -- the client was not started.
    echo   Scroll up for the compiler error. Nothing was launched, so your last
    echo   working build is still in place.
    echo.
    pause
    exit /b 1
)

:launch
echo.
echo === Starting the client ===
echo   World and options: run\
echo   Close the game window when you are done.
echo.

call "%GRADLEW%" runClient
if errorlevel 1 (
    echo.
    echo   The client exited with an error. If no window ever opened, try a
    echo   clean build:  gradlew.bat clean build
    echo.
    pause
    exit /b 1
)

endlocal
