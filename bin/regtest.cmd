    @echo off
    setlocal
    if "%1" == "/doregtest" goto regtest

    set scriptpath=%0
    set scriptpath=%scriptpath:\regtest.cmd=%
    set scriptpath=%scriptpath:\regtest=%

    set oopsc=OOPSC.jar
    set ext=compare

:options
    if "%1" == "" goto check
    if not "%1" == "/c" goto next1
    shift
    set oopsc=%1
    goto cont

:next1
    if not "%1" == "/o" goto next2
    set ext=out
    goto cont

:next2
    if "%1" == "/?" goto usage
    set param=%1
    if not "%param:~0,1%" == "/" goto next3
    echo Unbekannter Parameter: %1
    goto usage

:next3
    set filename=%1

:cont
    shift
    goto options

:check
    if not "%filename%" == "" goto ok
    echo Keine Dateien angegeben
    goto usage

:ok
    for %%i in (%filename%) do call %scriptpath%\regtest /doregtest %%i
    goto cleanup

:regtest
    set name=%~n2
    if not %ext% == compare goto generate
    <nul (set /p dummy=Testing %2 ... )
    goto compile

:generate
    echo Generating %name%.out ...

:compile
    java -ea -Dfile.encoding=UTF-8 -jar "%scriptpath%\%oopsc%" %2 %name%.asm >%name%.%ext%
    if errorlevel 1 goto compare
    echo abc | java -Dfile.encoding=UTF-8 -jar "%scriptpath%\OOPSVM.jar" %name%.asm >%name%.%ext%
    echo xyz | java -Dfile.encoding=UTF-8 -jar "%scriptpath%\OOPSVM.jar" %name%.asm >>%name%.%ext%

:compare
    if not %ext% == compare goto delete
    fc %name%.out %name%.compare >nul
    if errorlevel 1 goto failed
    echo Ok
    del %name%.compare
:delete
    if exist %name%.asm del %name%.asm
    goto end

:failed
    echo Failed (Check %name%.out/.compare)
    goto end

:usage
    set x=?
    echo Nutzung: regtest {optionen} ^<oops-dateien^> {optionen}
    echo   Optionen:
    echo     /c ^<oopsc-jar^>  Eine bestimmte Version des OOPS-Compilers nutzen
    echo     /o              Erzeuge .out-Datei statt sie zu vergleichen
    echo     / ?              Zeige diese Hilfe

:cleanup
    endlocal
:end
