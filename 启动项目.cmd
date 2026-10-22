@echo off
rem ===================================================================
rem  Black-horse Intelligent Learning Auxiliary System - launcher
rem  Double-click the shortcut "启动项目" (or this file) to start.
rem ===================================================================
setlocal
cd /d "%~dp0"

rem ---- 窗口标题（必须在 chcp 之前设置，否则中文会被改码页打断） ----
title 黑马智能学习辅助系统 · COMP603 Project 1

rem ---- 1. Locate JDK 21 (Temurin installed under D:\dev) ----
if exist "D:\dev\jdk21-current\bin\java.exe" (
    set "JAVA_HOME=D:\dev\jdk21-current"
    set "PATH=D:\dev\jdk21-current\bin;%PATH%"
) else (
    echo [!] JDK 21 not found at D:\dev\jdk21-current
    echo     The project requires Java 21. Please install it or edit this file.
    pause
    exit /b 1
)

rem ---- 2. Console must be UTF-8 for Chinese output ----
chcp 65001 >nul

rem ---- 3. Build the jar on first run (or after source changes) ----
if not exist "target\learning-assistant.jar" (
    echo [*] target\learning-assistant.jar not found, building with Maven ...
    call "D:\dev\apache-maven-3.9.16\bin\mvn.cmd" -s "D:\dev\settings.xml" -B -q clean package -DskipTests
    if errorlevel 1 (
        echo [!] Build failed. See the messages above.
        pause
        exit /b 1
    )
    echo [*] Build finished.
)

rem ---- 4. Run (must start from project root: data/ is a relative path) ----
echo [*] Starting the system ... (type Q at any menu to quit)
echo.
"%JAVA_HOME%\bin\java.exe" -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -jar "target\learning-assistant.jar"

if errorlevel 1 (
    echo.
    echo [!] The program exited with an error.
    pause
    exit /b 1
)

endlocal
