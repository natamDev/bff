@ECHO OFF
SETLOCAL
SET WRAPPER_DIR=%~dp0
SET JAR=%WRAPPER_DIR%\.mvn\wrapper\maven-wrapper.jar
SET PROPS=%WRAPPER_DIR%\.mvn\wrapper\maven-wrapper.properties
IF NOT EXIST "%JAR%" (
  FOR /F "tokens=2 delims==" %%A IN ('findstr /R "^wrapperUrl=" "%PROPS%"') DO SET WRAPPER_URL=%%A
  ECHO Downloading Maven Wrapper JAR from: %WRAPPER_URL%
  powershell -Command "Invoke-WebRequest -UseBasicParsing -Uri '%WRAPPER_URL%' -OutFile '%JAR%'"
)
SET JAVA_EXE=java
IF NOT "%JAVA_HOME%"=="" SET JAVA_EXE=%JAVA_HOME%\bin\java.exe
"%JAVA_EXE%" -classpath "%JAR%" -Dmaven.multiModuleProjectDirectory="%WRAPPER_DIR%" org.apache.maven.wrapper.MavenWrapperMain %*
