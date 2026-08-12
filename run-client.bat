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
REM  To reach the Twilight Forest once you are in game, make a portal. There is
REM  no frame to build and nothing to craft -- you make an offering to a
REM  clearing, which is Twilight Forest 1.7.1's own mechanism:
REM
REM    1. Dig a 2x2 hole one block deep and fill it with water.
REM    2. The twelve blocks ringing that pool must all be GRASS, and every one
REM       of them must have something growing on top: a flower, a mushroom or
REM       tall grass. Any mix will do.
REM    3. Drop a DIAMOND into the water. Lightning strikes and the pool becomes
REM       the portal. (The diamond is not consumed -- that is upstream's, not an
REM       oversight here.)
REM
REM  Step 2 is the fiddly one. If nothing happens, that is almost always what is
REM  wrong: a corner block that is dirt rather than grass, or a bare ring block
REM  with nothing growing on it. A natural flowery meadow needs very little work.
REM
REM  Coming back needs no effort -- arriving builds a return portal for you, and
REM  walking into either one takes you the other way.
REM
REM  NOTE: earlier revisions of this file described a mossy cobblestone frame and
REM  a red-mushroom config switch. Both are gone. Those belonged to the 1.p4
REM  port, which had no portal block at all and had to invent one; 1.7.1 ships a
REM  real BlockTFPortal and the invented pair was retired with it. MUSHROOM_PORTAL
REM  in run\config\twilightforest.cfg does nothing now.
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
