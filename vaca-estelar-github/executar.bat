@echo off
setlocal
cd /d "%~dp0"
where javac >nul 2>nul
if errorlevel 1 (
    echo Instale um JDK 17 ou superior e adicione-o ao PATH. Reabra o terminal.
    pause
    exit /b 1
)
if not exist out mkdir out
javac -encoding UTF-8 -d out src\br\com\vacaestelar\Main.java src\br\com\vacaestelar\model\*.java src\br\com\vacaestelar\animation\*.java src\br\com\vacaestelar\ui\*.java
if errorlevel 1 (
    pause
    exit /b 1
)
java -cp out br.com.vacaestelar.Main
if errorlevel 1 pause
