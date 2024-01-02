echo off

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
for %%i in (%SERVER_HOME%\lib\*.jar) do (
 set CLASSPATH=!CLASSPATH!;%%i
)

set SERVEROPT= -Dserver.home=%SERVER_HOME% -Xmx35g

set JAVA=java

%JAVA% %SERVEROPT% -classpath %CLASSPATH% com.tongtech.proxy.SM3 %1

:end

endlocal

pause