@echo off

rem  $Id: HardwareSimulator.bat,v 1.3 2014/05/10 00:52:43 marka Exp $
rem  mark.armbrust@pobox.com

setlocal
if not "%2"=="" goto :USAGE
if "%~1"=="/?" (
:USAGE
  echo Usage:
  echo     HardwareSimulator             Starts the Hardware Simulator in
  echo                                   interactive mode.
  echo     HardwareSimulator FILE.tst    Starts the Hardware Simulator and runs the
  echo                                   FILE.tst test script.  The success/failure
  echo                                   message is printed to the command console.
  exit -b
)
if not "%~1"=="" (
  set "_arg1=%~f1"
)
pushd "%~dp0"
if "%~1"=="" (
  start javaw -jar bin/HardwareSimulator.jar
) else (
rem  echo Running "%_arg1%"
  java -jar bin/HardwareSimulator.jar "%_arg1%"
)
popd
