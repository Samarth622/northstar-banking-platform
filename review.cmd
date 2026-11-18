@echo off
setlocal
pushd "%~dp0"
if errorlevel 1 exit /b 1

powershell.exe -NoProfile -ExecutionPolicy Bypass -File ".\.github\scripts\review-codebase.ps1" -Model "qwen2.5-coder:1.5b"
set "review_exit_code=%ERRORLEVEL%"

popd
exit /b %review_exit_code%
