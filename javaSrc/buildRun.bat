@echo off

cls

call build.bat

if errorlevel 1 (
    pause
    exit /b 1
)

call run.bat