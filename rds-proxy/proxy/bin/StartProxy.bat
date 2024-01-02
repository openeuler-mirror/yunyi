rem echo off

setlocal

cd /d "%~dp0"

set "SERVER_HOME=%cd%"
if exist "%SERVER_HOME%\etc\proxy.xml" goto okHome

set "SERVER_HOME=%cd..%"
if exist "%SERVER_HOME%\etc\proxy.xml" goto okHome
set "SERVER_HOME=%~dp0\.."
:gotHome

if exist "%SERVER_HOME%\etc\proxy.xml" goto okHome
echo The SERVER_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:okHome


set CLASSPATH=
setlocal enabledelayedexpansion
for %%i in ("%SERVER_HOME%\lib\*.jar") do (
 set CLASSPATH=!CLASSPATH!;%%i
)

set SERVEROPT= -Dserver.home="%SERVER_HOME%" -Xmx35g -Djdk.tls.rejectClientInitiatedRenegotiation=true

set JAVA=%SERVER_HOME%\jdk\bin\java.exe

if exist "%JAVA%" goto do_java

FOR /F "delims=" %%I IN ("java.exe") DO (if exist %%~$PATH:I (set JAVA=java) else (set JAVA="%JAVA_HOME%\bin\java.exe"))

:do_java

cd %SERVER_HOME%

"%JAVA%" %SERVEROPT% -classpath "%CLASSPATH%" com.tongtech.proxy.Proxy

rem if ERRORLEVEL 1 pause

:end

@echo

endlocal
