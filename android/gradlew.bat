@rem Minimal Gradle wrapper launcher. Requires gradle\wrapper\gradle-wrapper.jar.
@rem Generate it once with:  gradle wrapper --gradle-version 8.7
@echo off
setlocal
set DIR=%~dp0
set JAR=%DIR%gradle\wrapper\gradle-wrapper.jar
if not exist "%JAR%" (
  echo gradle-wrapper.jar missing. Run: gradle wrapper --gradle-version 8.7 1>&2
  exit /b 1
)
if defined JAVA_HOME (set JAVA_EXE=%JAVA_HOME%\bin\java.exe) else (set JAVA_EXE=java)
"%JAVA_EXE%" -classpath "%JAR%" org.gradle.wrapper.GradleWrapperMain %*
