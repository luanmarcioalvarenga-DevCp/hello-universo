@echo off
setlocal
cd /d "%~dp0"
where javac >nul 2>nul
if errorlevel 1 (
    echo Instale um JDK 17 ou superior e adicione a pasta bin ao PATH.
    echo Depois feche e abra o terminal e execute este arquivo novamente.
    pause
    exit /b 1
)
if not exist out mkdir out
javac -encoding UTF-8 -d out src\HelloUniverso.java
if errorlevel 1 (
    pause
    exit /b 1
)
java -cp out HelloUniverso
if errorlevel 1 pause
