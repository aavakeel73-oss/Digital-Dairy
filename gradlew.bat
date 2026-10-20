@echo off
setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve any "." and ".." in APP_HOME to make it shorter and cleaner
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fhi

set JAVACMD=
if "%JAVA_HOME%" == "" goto gnu-java_set

set JAVACMD=%JAVA_HOME%\bin\java.exe
if exist "%JAVACMD%" goto execute

echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
goto fail

:gnu-java_set
set JAVACMD=java
which java >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
goto fail

:execute
"@%JAVACMD%" org.gradle.wrapper.GradleWrapperMain %*

:fail
exit /b 1

