@echo off

rem  $Id: VMEmulator.bat,v 1.3 2014/05/10 00:51:55 marka Exp $
rem  mark.armbrust@pobox.com

setlocal 
if not "%2"=="" goto :USAGE
if "%~1"=="/?" (
:USAGE
  echo Usage:
  echo     VMEmulator             Starts the VM Emulator in interactive mode.
  echo     VMEmulator FILE.tst    Starts the VM Emulator and runs the FILE.tst test
  echo                            script.  The success/failure message is
  echo                            printed to the command console.
  exit -b
)
if not "%~1"=="" (
  set "_arg1=%~f1"
)
pushd "%~dp0"
if "%~1"=="" (
  start javaw -jar bin/VMEmulator.jar
) else (
rem  echo Running "%_arg1%"
  if "%N2T_VM_USE_BUILTINS%"=="" set N2T_VM_USE_BUILTINS=yes
  java -jar bin/VMEmulator.jar "%_arg1%"
)
popd
