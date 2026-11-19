@echo off
rem Compile le projet sans Maven et crée PharmacieStock.jar
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out
dir /s /b src\main\java\*.java > sources.txt
javac --release 17 -encoding UTF-8 -d out @sources.txt
if errorlevel 1 (
    del sources.txt
    echo Erreur de compilation.
    pause
    exit /b 1
)
del sources.txt
jar --create --file PharmacieStock.jar --main-class pharmacie.Main -C out .
echo Compilation terminee : PharmacieStock.jar
pause
