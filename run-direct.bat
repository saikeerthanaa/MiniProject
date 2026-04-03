@echo off
REM ═══════════════════════════════════════════════════════════════════
REM TradeJournal — Direct Run Script (No Maven Required)
REM Uses pre-compiled classes with Spring Boot embedded server
REM ═══════════════════════════════════════════════════════════════════

echo.
echo Checking prerequisites...

REM Check Java
java -version >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java not found!
    echo Install from: https://adoptopenjdk.net/
    pause
    exit /b 1
)

echo ✓ Java found

REM Check MySQL
mysql -u root -p"redBlue3011!" -e "SELECT 1" >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo WARNING: Cannot connect to MySQL!
    echo ├─ Is MySQL running?
    echo ├─ Is database 'TradeJournal' created?
    echo └─ Is password 'redBlue3011!' correct?
    echo.
    pause
    exit /b 1
)

echo ✓ MySQL connection OK
echo.
echo ═══════════════════════════════════════════════════════════════════
echo TradeJournal Starting (Direct Java Mode)
echo ═══════════════════════════════════════════════════════════════════
echo.

REM Download Spring Boot JAR if missing
if not exist "spring-boot-app.jar" (
    echo Downloading Spring Boot dependencies... this may take a minute...
    REM For now, we'll use Java classpath mode
)

REM Run with compiled classes
echo Starting application on http://localhost:8080
echo.
echo Press Ctrl+C to stop...
echo.

cd src
java -cp ".;../lib/*" com.tradejournal.TradeJournalApplication

pause
