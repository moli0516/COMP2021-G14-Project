@echo off
setlocal DisableDelayedExpansion
cd /d "%~dp0"
set "MODE=cli"
set "HTML="
set "TXT="
if /i "%~1"=="help" goto help
if /i "%~1"=="--help" goto help
if /i "%~1"=="-h" goto help
if /i "%~1"=="web" goto webMode
if /i "%~1"=="cli" goto cliMode
goto arguments
:webMode
set "MODE=web"
shift
goto arguments
:cliMode
shift
:arguments
if "%~1"=="" goto defaults
if "%~1"=="-html" goto flags
if "%~1"=="-txt" goto flags
if not "%~2"=="" goto badArgs
set "PREFIX=%~1"
if "%PREFIX:~0,1%"=="-" goto badArgs
set "HTML=%PREFIX%.html"
set "TXT=%PREFIX%.txt"
goto prepare
:flags
if "%~1"=="" goto flagsDone
if "%~2"=="" goto badArgs
if "%~1"=="-html" goto htmlFlag
if "%~1"=="-txt" goto txtFlag
goto badArgs
:htmlFlag
if defined HTML goto badArgs
set "HTML=%~2"
shift
shift
goto flags
:txtFlag
if defined TXT goto badArgs
set "TXT=%~2"
shift
shift
goto flags
:flagsDone
if not defined HTML goto badArgs
if not defined TXT goto badArgs
goto prepare
:defaults
set "HTML=logs\%MODE%.html"
set "TXT=logs\%MODE%.txt"
:prepare
where javac >nul 2>&1
if errorlevel 1 goto noJdk
where java >nul 2>&1
if errorlevel 1 goto noJdk
set "MAIN=hk.edu.polyu.comp.comp2021.crobs.Application"
if "%MODE%"=="cli" goto directories
set "MAIN=hk.edu.polyu.comp.comp2021.crobs.web.WebApplication"
if not exist "src\hk\edu\polyu\comp\comp2021\crobs\web\WebApplication.java" goto noWeb
if not exist "webui\index.html" goto noWeb
if not exist "webui\app.js" goto noWeb
if not exist "webui\style.css" goto noWeb
:directories
for %%F in ("%HTML%" "%TXT%") do if not exist "%%~dpF" mkdir "%%~dpF"
:chooseBuild
set "BUILD_DIR=%TEMP%\crobs-run-%RANDOM%-%RANDOM%"
if exist "%BUILD_DIR%" goto chooseBuild
mkdir "%BUILD_DIR%\classes"
if errorlevel 1 exit /b 1
rem Forward slashes prevent Java argfile backslash escape interpretation.
> "%BUILD_DIR%\sources.txt" (
    for /r "src" %%F in (*.java) do @call :sourceLine "%%~fF"
)
javac -encoding UTF-8 -d "%BUILD_DIR%\classes" @"%BUILD_DIR%\sources.txt"
if errorlevel 1 goto buildFailed
echo Mode: %MODE%
echo HTML log: "%HTML%"
echo TXT log: "%TXT%"
if "%MODE%"=="web" goto runWeb
java -cp "%BUILD_DIR%\classes" %MAIN% -html "%HTML%" -txt "%TXT%"
goto finished
:runWeb
java --add-modules jdk.httpserver -cp "%BUILD_DIR%\classes" %MAIN% -html "%HTML%" -txt "%TXT%"
:finished
set "RESULT=%ERRORLEVEL%"
rmdir /s /q "%BUILD_DIR%"
exit /b %RESULT%
:buildFailed
rmdir /s /q "%BUILD_DIR%"
exit /b 1
:sourceLine
set "SOURCE=%~1"
echo "%SOURCE:\=/%"
exit /b
:noJdk
echo Error: Install JDK 25 and put java and javac on PATH.
exit /b 1
:noWeb
echo Error: Web UI files missing. Apply the Web UI patch first.
exit /b 1
:badArgs
call :help
exit /b 2
:help
echo Usage: run.bat [cli^|web] [log-prefix]
echo        run.bat [cli^|web] -html file -txt file
echo Defaults: cli mode; logs\cli.html and logs\cli.txt
exit /b 0
