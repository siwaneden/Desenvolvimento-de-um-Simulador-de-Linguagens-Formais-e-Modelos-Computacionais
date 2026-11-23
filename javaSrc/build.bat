@echo off
echo ===== BUILD =====

cd /d "%~dp0"

if not exist out mkdir out

setlocal enabledelayedexpansion
set "FILES=Main.java"

for /R src %%F in (*.java) do (
    set "FILES=!FILES! "%%F""
)

if not exist Main.java (
    echo ERRO: Main.java nao encontrado!
    exit /b 1
)

javac -encoding UTF-8 -cp "lib/*" -d out !FILES!

if errorlevel 1 (
    echo.
    echo ERRO NA COMPILACAO!
    exit /b 1
)

echo.
echo BUILD CONCLUIDO!