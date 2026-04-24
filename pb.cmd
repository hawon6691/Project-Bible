@echo off
setlocal

set "PB_ROOT=%~dp0"
set "PYTHONPATH=%PB_ROOT%Tools\cli\src;%PYTHONPATH%"

python -m pbcli.main %*
exit /b %ERRORLEVEL%
