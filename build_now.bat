@echo off
echo Starting build...
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot"
echo JAVA_HOME=%JAVA_HOME%
"%JAVA_HOME%\bin\java.exe" -version
echo Running Gradle...
call "%~dp0gradlew.bat" build --info
echo Build complete!
pause
