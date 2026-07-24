@echo off
setlocal enabledelayedexpansion

:: 获取当前 Java 主版本号（支持 1.8/9/17/21 等格式）
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set fullVer=%%g
    set fullVer=!fullVer:"=!
)
:: 提取主版本
for /f "delims=.- tokens=1-2" %%v in ("!fullVer!") do (
    if "%%v"=="1" (set major=%%w) else (set major=%%v)
)

:: 判断
if !major! GEQ 17 (
    echo [INFO] Java 17+ detected, starting growing-mdal...
    java -jar growing-mdal-1.0.3.jar
) else (
    echo [ERROR] Java 17 or higher is required. Current version: !fullVer!
    pause
)
