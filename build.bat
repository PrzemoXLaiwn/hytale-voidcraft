@echo off
setlocal
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot"
set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
set "CLASSPATH=%~dp0gradle\wrapper\gradle-wrapper.jar"
"%JAVA_EXE%" "-Xmx64m" "-Xms64m" "-Dorg.gradle.appname=gradlew" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain build
endlocal
