@ECHO off

SetLocal EnableDelayedExpansion

set WORKING_DIR=%~dp0
set LIB=%WORKING_DIR%lib\
set MIGRATOR_CP=

for /F %%a in ('dir "%LIB%" /a /b /-p /o') do set MIGRATOR_CP=%LIB%%%a;!MIGRATOR_CP!

java -cp "%MIGRATOR_CP%" org.apache.ibatis.migration.Migrator %*

EndLocal