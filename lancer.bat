@echo off
rem Lance l'application (le pilote mssql-jdbc doit être dans le dossier lib)
cd /d "%~dp0"
if not exist config.properties (
    copy config.exemple.properties config.properties >nul
    echo config.properties cree a partir du modele : renseignez le mot de passe de la base.
    notepad config.properties
)
if not exist PharmacieStock.jar call compiler.bat
java -cp "PharmacieStock.jar;lib\*" pharmacie.Main
