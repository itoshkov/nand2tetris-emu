@echo off

rem  $Id: CPUEmulator.bat,v 1.3 2014/05/10 00:52:43 marka Exp $
rem  mark.armbrust@pobox.com

setlocal
if not "%2"=="" goto :USAGE
if "%~1"=="/?" (
:USAGE
  echo Usage:
  echo     CPUEmulator             Starts the CPU Emulator in interactive mode.
  echo     CPUEmulator FILE.tst    Starts the CPU Emulator and runs the FILE.tst
  echo                             test script.  The success/failure message
  echo                             is printed to the command console.
  exit -b
)
if not "%~1"=="" (
  set "_arg1=%~f1"
)
pushd "%~dp0"
if "%~1"=="" (
  start javaw -jar bin/CPUEmulator.jar
) else (
rem  echo Running "%_arg1%"
  java -jar bin/CPUEmulator.jar "%_arg1%"
)
popd
