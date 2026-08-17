@echo off
setlocal
set "GRADLE_VERSION=9.3.1"
set "CACHE_DIR=%USERPROFILE%\.cache\namaz-vakti\gradle-%GRADLE_VERSION%"
set "GRADLE_BIN=%CACHE_DIR%\gradle-%GRADLE_VERSION%\bin\gradle.bat"
if not exist "%GRADLE_BIN%" (
  if not exist "%CACHE_DIR%" mkdir "%CACHE_DIR%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%CACHE_DIR%\gradle.zip'"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force '%CACHE_DIR%\gradle.zip' '%CACHE_DIR%'"
)
call "%GRADLE_BIN%" -p "%~dp0" %*
endlocal
