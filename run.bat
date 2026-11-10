@echo off
setlocal
cd /d "%~dp0"

where javac >nul 2>&1
if errorlevel 1 (
    echo Error: Install JDK 25 and ensure javac is on PATH.
    exit /b 1
)

where java >nul 2>&1
if errorlevel 1 (
    echo Error: Install JDK 25 and ensure java is on PATH.
    exit /b 1
)

:chooseBuild
set "BUILD_DIR=%TEMP%\crobs-run-%RANDOM%-%RANDOM%"
if exist "%BUILD_DIR%" goto chooseBuild

mkdir "%BUILD_DIR%\classes"
if errorlevel 1 exit /b 1

> "%BUILD_DIR%\sources.txt" (
    for /r "src" %%F in (*.java) do @echo "%%~fF"
)

javac -encoding UTF-8 -d "%BUILD_DIR%\classes" @"%BUILD_DIR%\sources.txt"
if errorlevel 1 (
    rmdir /s /q "%BUILD_DIR%"
    exit /b 1
)

java -cp "%BUILD_DIR%\classes" hk.edu.polyu.comp.comp2021.crobs.Application %*
set "RESULT=%ERRORLEVEL%"

rmdir /s /q "%BUILD_DIR%"
exit /b %RESULT%