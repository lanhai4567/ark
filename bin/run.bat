@echo off
cd ..
:noenvreset
setlocal enabledelayedexpansion

set CLSNAME=com.etone.ark.kernel.Runner
set LIB=
for %%f in (lib\*.jar) do set LIB=!LIB!;%%f
rem echo libs: %LIB%H:~1%

java -Xmx128m -Xms128m -cp "%LIB%" %CLSNAME% ARK_KERNEL