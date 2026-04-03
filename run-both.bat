@echo off
REM ═══════════════════════════════════════════════════════════════════
REM TradeJournal - Start Both Backend and JavaFX Desktop App
REM ═══════════════════════════════════════════════════════════════════

setlocal enabledelayedexpansion

cd /d "c:\Users\saik3\OneDrive\Pictures\Desktop\College\DBS MiniProject"

echo.
echo ╔═══════════════════════════════════════════════════════════════╗
echo ║         TradeJournal - Backend + Desktop App Launcher        ║
echo ╚═══════════════════════════════════════════════════════════════╝
echo.

REM Check if JAR exists
if not exist "target\trade-journal-1.0.0.jar" (
    echo ERROR: JAR file not found!
    echo Please run: mvn clean package -DskipTests
    pause
    exit /b 1
)

echo Starting processes...
echo.

REM Start Backend in new window
echo [1/2] Starting Spring Boot Backend on port 8080...
start "TradeJournal Backend" cmd /k "cd /d "c:\Users\saik3\OneDrive\Pictures\Desktop\College\DBS MiniProject" && java -jar target\trade-journal-1.0.0.jar"

REM Wait 5 seconds for backend to start
timeout /t 5 /nobreak

REM Start JavaFX in new window
echo [2/2] Starting JavaFX Desktop App...
start "TradeJournal Desktop" cmd /k "cd /d "c:\Users\saik3\OneDrive\Pictures\Desktop\College\DBS MiniProject" && "C:\Program Files\apache-maven-3.9.14-bin\apache-maven-3.9.14\bin\mvn.cmd" javafx:run"

echo.
echo ✓ Both applications started!
echo.
echo   Backend:  http://localhost:8080
echo   Desktop:  JavaFX window (should open automatically)
echo.
echo Press any key to close this window... (apps will keep running)
pause
